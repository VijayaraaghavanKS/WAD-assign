package com.ssn.orderservice.service;

import com.ssn.orderservice.model.Address;
import com.ssn.orderservice.model.Coupon;
import com.ssn.orderservice.model.CheckoutRequest;
import com.ssn.orderservice.model.Order;
import com.ssn.orderservice.model.OrderLine;
import com.ssn.orderservice.repository.CouponRepository;
import com.ssn.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    static final List<String> STATUSES = List.of("PLACED", "PACKED", "SHIPPED", "DELIVERED");
    static final List<String> PAYMENTS = List.of("UPI", "CARD", "COD");
    static final int FREE_DELIVERY_AT = 5000;
    static final int DELIVERY_FEE = 99;
    static final int GST_RATE = 18;

    private final OrderRepository orders;
    private final RestTemplate rest;
    private final CouponRepository coupons;

    @Value("${cart.service.url}")
    private String cartUrl;

    @Value("${product.service.url}")
    private String productUrl;

    public CheckoutService(OrderRepository orders, RestTemplate rest, CouponRepository coupons) {
        this.orders = orders;
        this.rest = rest;
        this.coupons = coupons;
    }

    // Turns the caller's cart into an order. Stock is reserved one line at a time;
    // if one line is out of stock, the lines already reserved are released again.
    public Order checkout(String authorization, Map<String, String> user, CheckoutRequest request) {
        List<OrderLine> lines = fetchCart(authorization);
        if (lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }
        Address address = request == null ? null : request.address();
        if (address == null || blank(address.name()) || blank(address.line1()) || blank(address.city())
                || address.pin() == null || !address.pin().matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add a delivery address with a 6-digit PIN");
        }
        String payment = request.payment() == null ? "UPI" : request.payment();
        if (!PAYMENTS.contains(payment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment must be one of " + PAYMENTS);
        }

        Integer percent = null;
        String code = null;
        if (request.coupon() != null && !request.coupon().isBlank()) {
            code = request.coupon().trim().toUpperCase();
            percent = coupons.findById(code).map(Coupon::getPercent).orElse(null);
            if (percent == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That coupon code is not valid");
            }
        }

        List<OrderLine> reserved = new ArrayList<>();
        try {
            for (OrderLine line : lines) {
                reserve(line);
                reserved.add(line);
            }
        } catch (HttpClientErrorException e) {
            reserved.forEach(this::release);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Some items are out of stock. Please update your cart.");
        }

        double subtotal = Math.round(lines.stream().mapToDouble(l -> l.price() * l.quantity()).sum() * 100) / 100.0;
        double discount = percent == null ? 0 : Math.round(subtotal * percent) / 100.0;
        double afterDiscount = subtotal - discount;
        double deliveryFee = afterDiscount >= FREE_DELIVERY_AT ? 0 : DELIVERY_FEE;

        Order order = new Order();
        order.setUserId(user.get("id"));
        order.setUsername(user.get("username"));
        order.setItems(lines);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setCouponCode(code);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(Math.round((afterDiscount + deliveryFee) * 100) / 100.0);
        order.setTax(Math.round(order.getTotal() * GST_RATE / (100 + GST_RATE) * 100) / 100.0);
        order.setStatus("PLACED");
        order.setPlacedAt(Instant.now());
        order.setShippingAddress(address);
        order.setPaymentMethod(payment);
        order.setInvoiceNumber("INV-" + String.format("%08d", System.currentTimeMillis() % 100_000_000L));

        Order saved = orders.save(order);
        clearCart(authorization);
        return saved;
    }

    // Coupons are kept in MongoDB so the admin can add or remove them.
    public List<Coupon> listCoupons() {
        return coupons.findAll();
    }

    public Coupon saveCoupon(String code, int percent) {
        String key = code == null ? "" : code.trim().toUpperCase();
        if (key.isEmpty() || percent < 1 || percent > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Give a code and a percentage from 1 to 90");
        }
        return coupons.save(new Coupon(key, percent));
    }

    public void deleteCoupon(String code) {
        coupons.deleteById(code);
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    public List<Order> history(String userId) {
        return orders.findByUserIdOrderByPlacedAtDesc(userId);
    }

    public List<Order> all() {
        return orders.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "placedAt"));
    }

    public Order setStatus(String id, String status) {
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be one of " + STATUSES);
        }
        Order order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such order"));
        order.setStatus(status);
        return orders.save(order);
    }

    // Sales numbers for the admin chart: totals, status counts, last 14 days, top products.
    public Map<String, Object> stats() {
        List<Order> all = orders.findAll();

        double revenue = all.stream().mapToDouble(Order::getTotal).sum();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        STATUSES.forEach(s -> byStatus.put(s, 0L));
        all.forEach(o -> byStatus.merge(o.getStatus() == null ? "PLACED" : o.getStatus(), 1L, Long::sum));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<Order> onDay = all.stream()
                    .filter(o -> o.getPlacedAt().atZone(ZoneOffset.UTC).toLocalDate().equals(day))
                    .toList();
            days.add(Map.<String, Object>of("day", day.toString(),
                    "revenue", onDay.stream().mapToDouble(Order::getTotal).sum(),
                    "orders", onDay.size()));
        }

        List<Map<String, Object>> top = all.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(OrderLine::productName,
                        Collectors.summingDouble(l -> l.price() * l.quantity())))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("name", e.getKey(), "revenue", e.getValue()))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenue", revenue);
        result.put("orderCount", all.size());
        result.put("averageOrderValue", all.isEmpty() ? 0 : revenue / all.size());
        result.put("byStatus", byStatus);
        result.put("lastFourteenDays", days);
        result.put("topProducts", top);
        return result;
    }

    private List<OrderLine> fetchCart(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        List<Map<String, Object>> cart = rest.exchange(cartUrl + "/api/cart", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        List<OrderLine> lines = new ArrayList<>();
        for (Map<String, Object> item : cart) {
            lines.add(new OrderLine(
                    (String) item.get("productId"),
                    (String) item.get("productName"),
                    ((Number) item.get("price")).doubleValue(),
                    ((Number) item.get("quantity")).intValue(),
                    (String) item.get("size"),
                    (String) item.get("colour")));
        }
        return lines;
    }

    private void reserve(OrderLine line) {
        rest.postForObject(productUrl + "/api/products/" + line.productId() + "/reserve",
                Map.of("quantity", line.quantity()), Map.class);
    }

    private void release(OrderLine line) {
        rest.postForObject(productUrl + "/api/products/" + line.productId() + "/release",
                Map.of("quantity", line.quantity()), Map.class);
    }

    private void clearCart(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        rest.exchange(cartUrl + "/api/cart", HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
    }
}
