package com.ssn.cartbackend.service;

import com.ssn.cartbackend.model.*;
import com.ssn.cartbackend.repository.CartRepository;
import com.ssn.cartbackend.repository.OrderRepository;
import com.ssn.cartbackend.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    // Coupon codes and their percentage off. Same codes the microservice version accepts.
    static final Map<String, Integer> COUPONS = Map.of("SAVE10", 10, "WELCOME20", 20);
    static final List<String> STATUSES = List.of("PLACED", "PACKED", "SHIPPED", "DELIVERED");
    static final List<String> PAYMENTS = List.of("UPI", "CARD", "COD");
    static final int FREE_DELIVERY_AT = 5000;
    static final int DELIVERY_FEE = 99;
    static final int GST_RATE = 18;

    private final CartRepository carts;
    private final ProductRepository products;
    private final OrderRepository orders;

    public CheckoutService(CartRepository carts, ProductRepository products, OrderRepository orders) {
        this.carts = carts;
        this.products = products;
        this.orders = orders;
    }

    // Turns the user's cart into an order. If one line is out of stock, the lines
    // already taken from stock are put back before the error is returned.
    public Order checkout(String userId, String username, CheckoutRequest request) {
        List<CartItem> cart = carts.findByUserId(userId);
        if (cart.isEmpty()) {
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
            percent = COUPONS.get(code);
            if (percent == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That coupon code is not valid");
            }
        }

        List<CartItem> reserved = new ArrayList<>();
        for (CartItem item : cart) {
            if (!reserve(item.getProductId(), item.getQuantity())) {
                reserved.forEach(r -> release(r.getProductId(), r.getQuantity()));
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Some items are out of stock. Please update your cart.");
            }
            reserved.add(item);
        }

        List<OrderLine> lines = cart.stream()
                .map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity(), i.getSize(), i.getColour()))
                .toList();
        double subtotal = Math.round(lines.stream().mapToDouble(l -> l.price() * l.quantity()).sum() * 100) / 100.0;
        double discount = percent == null ? 0 : Math.round(subtotal * percent) / 100.0;
        double afterDiscount = subtotal - discount;
        double deliveryFee = afterDiscount >= FREE_DELIVERY_AT ? 0 : DELIVERY_FEE;

        Order order = new Order();
        order.setUserId(userId);
        order.setUsername(username);
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
        carts.deleteByUserId(userId);
        return saved;
    }

    public List<Order> history(String userId) {
        return orders.findByUserIdOrderByPlacedAtDesc(userId);
    }

    public List<Order> all() {
        return orders.findAll(Sort.by(Sort.Direction.DESC, "placedAt"));
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

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private boolean reserve(String productId, int quantity) {
        Product product = products.findById(productId).orElse(null);
        if (product == null || product.getQuantity() < quantity) return false;
        product.setQuantity(product.getQuantity() - quantity);
        products.save(product);
        return true;
    }

    private void release(String productId, int quantity) {
        products.findById(productId).ifPresent(p -> {
            p.setQuantity(p.getQuantity() + quantity);
            products.save(p);
        });
    }
}
