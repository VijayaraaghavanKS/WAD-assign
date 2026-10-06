# MaisonCart: Pitch Deck

A small online store built two ways for ICS1511 Web Application Development:

- **Exercise 8 (`ass8/`)**: one Spring Boot app, one database, one port. A monolith.
- **Exercise 9 (`ass9/`)**: four separate Spring Boot services, each with its own database and port. Microservices.

Both use the same Vue 3 storefront, the same API paths, and the same demo accounts, so you can compare the two designs side by side.

---

## 1. The idea in one minute

Shoppers browse a catalogue, save favourites, compare products, add items with the size and colour they want, and check out with a delivery address and a payment method. Admins see sales, track each product's performance, and move orders along. Developers watch every service's requests live.

The point is not to compete with Amazon on scale. The point is to show, in working code, how a real store is put together: login and roles, stock reserved safely at checkout, sale prices fixed at the moment of purchase, invoices with GST, and a console that makes the system easy to inspect.

---

## 2. Two versions, one storefront

| | Exercise 8 (monolith) | Exercise 9 (microservices) |
|---|---|---|
| Backend | 1 Spring Boot app | 4 Spring Boot apps |
| Port | 8082 | 8083 (product), 8084 (cart), 8085 (user), 8086 (order) |
| Database | `ShoppingCartDB` | One database per service (`E-CommDB`, `CartServiceDB`, `UserServiceDB`, `OrderServiceDB`) |
| Calls between parts | Direct method calls | HTTP calls over REST |
| Frontend | Same code; every call goes to port 8082 | Same code; each call goes to its service's port |
| Developer console | One log stream | Logs and metrics for each service, side by side |

The frontend is nearly identical in both. Only the base URLs differ.

---

## 3. Architecture

**Exercise 9 (microservices):**

```
                 Browser (Vue 3 + Pinia)
  /login  /  (shop)  /product/:id  /orders  /admin  /dev  /credits
        |      |            |          |        |      |
        +------+------------+----------+--------+------+
                              |
   +----------------+---------+--------+-----------------+
   |                |                  |                 |
User Service   Product Service     Cart Service      Order Service
  :8085           :8083               :8084             :8086
UserServiceDB   E-CommDB          CartServiceDB     OrderServiceDB
                  ^                    |                 |
                  +---- price lookup --+                 |
                  ^                                      |
                  +------- reserve / release stock ------+
   Every service asks User Service "who owns this token?" before it acts.
```

**Exercise 8 (monolith):**

```
Browser (Vue 3 + Pinia)  --->  Spring Boot app :8082
                                 |- user package     (login, tokens, roles)
                                 |- product package  (catalogue, reviews, stock)
                                 |- cart package     (per-user carts, variants)
                                 |- order package    (checkout, invoices, stats)
                                 |- dev package      (request log, metrics)
                                 |
                              ShoppingCartDB (MongoDB)
```

---

## 4. Services and what each one owns

| Service | Owns | Does not own |
|---|---|---|
| **User** (ass9 only) | Accounts, password hashes, login tokens, roles | Products, carts, orders |
| **Product** | Catalogue, categories, prices, sale %, stock, sizes, colours, specs, reviews | Who is buying |
| **Cart** | Each user's lines, with size and colour, and the sale price copied at add time | Stock levels (checked at checkout) |
| **Order** | Orders, addresses, payment method, delivery fee, GST, invoice number, status | Product or user records (copied in or looked up) |

In the monolith (ass8) the same four responsibilities are packages inside one app.

---

## 5. API endpoints

Anything that acts on a user needs `Authorization: Bearer <token>`. The token comes from `/api/auth/login`.

### User Service (ass9 `:8085`; ass8 the same paths on `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | Anyone | Creates a shopper account |
| POST | `/api/auth/login` | Anyone | Returns `{ token, username, role }` |
| GET | `/api/auth/me` | Any logged-in user | Who owns the token (used by other services) |

### Product Service (ass9 `:8083`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| GET | `/api/products` | Anyone | Lists products. Optional `?category=Audio` |
| GET | `/api/products/{id}` | Anyone | One product with sizes, colours, specs, description and reviews |
| POST | `/api/products/{id}/reviews` | Logged-in shopper | Posts a review (rating 1 to 5, headline, text) |
| POST | `/api/products` | Admin | Adds a product |
| PUT | `/api/products/{id}` | Admin | Edits name, price, stock, category, sale % |
| DELETE | `/api/products/{id}` | Admin | Removes a product |
| POST | `/api/products/{id}/reserve` | Order service (ass9 only) | Takes stock during checkout. 409 if short |
| POST | `/api/products/{id}/release` | Order service (ass9 only) | Puts stock back if checkout fails partway |

### Cart Service (ass9 `:8084`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| GET | `/api/cart` | Logged-in user | Their cart |
| POST | `/api/cart/add/{productId}` | Logged-in user | Body `{ "size", "colour" }` (optional). Same product in another size is a new line. Applies the sale price |
| PUT | `/api/cart/{cartItemId}` | Logged-in user | Sets a quantity |
| DELETE | `/api/cart/{cartItemId}` | Logged-in user | Removes one line |
| DELETE | `/api/cart` | Logged-in user | Empties the cart |

### Order Service (ass9 `:8086`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| POST | `/api/orders` | Logged-in user | Checkout. Body: `{ coupon, address: {name, phone, line1, city, state, pin}, payment: UPI / CARD / COD }`. Validates the address and coupon, takes stock, saves the invoice, empties the cart |
| GET | `/api/orders` | Logged-in user | Their orders, newest first, with address, payment, fees, GST and invoice |
| GET | `/api/orders/all` | Admin | Every order, with the customer's username |
| GET | `/api/orders/stats` | Admin | Revenue, order count, average order value, status counts, last 14 days, top products |
| PUT | `/api/orders/{id}/status` | Admin | Moves an order to PLACED, PACKED, SHIPPED or DELIVERED |

### Developer endpoints (for the console)

| Method | Path | Services | What it shows |
|---|---|---|---|
| GET | `/api/dev/logs` | User, Product, Cart, Order (ass9); one app (ass8) | The last 100 requests: method, path, status, time taken |
| GET | `/api/dev/metrics` | User, Product, Cart, Order (ass9); one app (ass8) | Total requests, errors, average time, uptime |

---

## 6. Shopper experience

**Shop (`/`)**
- Hero banner and a deal bar with a countdown to midnight.
- Category chips and a **Saved** filter that shows only wishlisted items.
- **Gift finder**: a budget slider. Products over the budget are hidden.
- **Recently viewed** strip, updated as you open products.
- Product cards with a heart (save for later), a Compare checkbox (up to three), a sale badge, stock warnings, and Add to cart.
- **Floating cart** at the bottom right shows the item count and total. Click it for a quick preview, then **View cart**. The header Cart link goes to the same page.
- **Compare tray** with a side-by-side table: price, list price, sale, category, stock.
- **Checkout form**: delivery address (remembered for next time) and payment method (UPI, card, cash on delivery). The PIN must be six digits.

**My cart (`/cart`)**
- A full page: each line shows the photo, name, size and colour, unit price, quantity stepper, save for later, and remove.
- Order summary: coupon box (`SAVE10` = 10% off, `WELCOME20` = 20% off), a free-delivery meter (free over ₹5,000, otherwise ₹99), the total, and GST shown as included. Checkout opens from here.

**Product page (`/product/:id`)**
- Photo, rating, price with list price and sale %, tax note, and stock level.
- **Size** and **colour** choices, saved on the cart line and the order.
- Quantity, Add to cart, and Save for later.
- Description, specification table, and warranty.
- Customer reviews: average rating, a 5-to-1 star breakdown, the review list, and a form to write one.
- Customers also viewed: other products in the same category.

**My orders (`/orders`)**
- Each order shows its invoice number, date, total, the address it shipped to, and a status pill.
- **View details** opens the invoice: status tracker with expected delivery, shipping address, billing (same address) and payment method, every item with its size and colour, and the price breakdown (subtotal, coupon, delivery, total paid, GST included).
- Per item: link to the product page, Save for later, and Add to cart. For the whole order: **Buy again** and **Print invoice**.

**Browsing without an account**
- The shop, product pages, offers and the cart are open to guests. A guest's cart is kept in the browser and moved into their account when they sign in.
- Checkout is the only step that asks for a sign-in. The cart then says so, and the shopper returns to the cart after signing in.

**Offers (`/offers`)** lists the coupon codes the Order Service really accepts (`SAVE10`, `WELCOME20`), each as a ticket with its terms, a copy button, and a "Use at cart" link. That link applies the code and shows the saving before payment. Standing terms (free delivery over ₹5,000, cash on delivery, GST inside the price) sit below.

**Photo credits (`/credits`)** lists the author, licence and source page of each product photo. The link is in the footer.

---

## 7. Roles and what each one sees

| Role | Demo account | Can see | Can do |
|---|---|---|---|
| Shopper | `shopper` and `shopper_asha`, `shopper_ravi`, ... / `demo123` | Shop, product pages, My orders | Browse, save, compare, cart, checkout, review |
| Admin | `admin` / `demo123` | Admin dashboard, product pages | Sales charts, product performance, orders, stock, prices |
| Developer | `developer` / `demo123` | Developer console, product pages | Watch logs and metrics for every service |

Route guards send each role to its own home page. The backend also checks roles on every admin write and read, so hiding a button is never the only protection.

---

## 8. Benefits for the admin

The admin page is a working dashboard, not an inventory form.

- **Four KPI cards**: revenue, order count, average order value, and products low on stock.
- **Revenue chart** for the last 14 days. Hover a bar for its revenue and order count.
- **Order status** breakdown across all orders.
- **Products, stock and performance** in one table: each product's price, sale %, stock, units sold, and revenue. Expand a row to see its orders, average order value, and share of revenue.
- **Orders table** with the customer's name and the amount paid (coupon shown). Change an order's status from a dropdown.
- **Low-stock panel** listing products to restock, lowest first.
- **Manage listings** in the same table: add a product with category, price, stock and sale %, edit inline, or delete.
- **Stock cannot go negative.** Checkout checks stock and fails with a clear message when an item sold out just before payment.
- **No redeploy is needed** to add a product, change a price, or move an order along.

---

## 9. Developer console and debugging

- **Service map (ass9).** Shows each service and whether it answers, checked every five seconds.
- **Request logs for every service.** The last 12 requests per service, with method, path, status, and time taken. Each service logs only its own requests, which is the trade-off microservices make against the monolith's single log.
- **Metrics per service.** Requests, average time, error rate, and uptime.
- **Endpoint reference.** Every endpoint, its service, and what it does.
- **Readable errors.** Each failure returns a status and a message ("Log in first", "Admins only", "Add a delivery address with a 6-digit PIN", "That coupon code is not valid", "Some items are out of stock"). The browser shows it in a toast.

**Where to look when something breaks:**
- Login fails: the User service on `:8085`, then the `users` collection.
- Price looks wrong: the sale % on the product, then the cart line (the price is copied when the item is added).
- Checkout fails: the message says whether the address, coupon, cart or stock was the problem. Stock is returned automatically if checkout stops partway.
- Order looks wrong: the order's invoice fields (subtotal, coupon, delivery fee, GST, total) are all stored on the order.
- Something slow: the console shows average time per service and the recent request list.

---

## 10. What makes this different from a typical student store

| Typical student project | MaisonCart |
|---|---|
| Anyone can change anything | Roles checked on every admin API call |
| Price changes rewrite old carts and orders | Sale price copied at add time; orders keep a snapshot of each line |
| Stock goes negative or is never checked | Stock reserved at checkout and returned if checkout fails partway |
| Order is just a total | Invoice with address, payment, delivery fee, coupon, GST and invoice number |
| Same product, one line | Size and colour are part of the cart line and the order line |
| Product page is a name and a price | Sizes, colours, specs, reviews, related items, and a review form |
| No sales view | Revenue chart, order statuses, and per-product performance |
| Login is decorative | Real tokens, demo accounts for each role, persisted session |
| Stock photos or none | Open-licensed photos with credits on a Credits page |

**Compared with large marketplaces:** we do not match their scale, recommendations, payment network or delivery network. We do show the same core ideas (sessions, roles, stock safety, invoices, reviews, sales reporting) in code a developer can read end to end.

---

## 11. Testing

### Automated tests (`./mvnw test`, all passing)

| Suite | Service | Tests | What it checks |
|---|---|---|---|
| `ProductControllerTest` | Product (ass9) | 4 | Listing, getting one product, and that creating a product needs an admin token |
| `CartServiceTest` and context test | Cart (ass9) | 3 | A new product gets its sale price. Adding the same product again only raises quantity. The application starts |
| `CartServiceTest` and context test | ass8 monolith | 3 | Same cart rules, inside the monolith |
| User, Order | ass9 | 0 | Compile and run checked. No automated tests yet |

### Real-life use cases

Checked means run against the live services with `curl` or the browser, and the result was seen.

| # | Scenario | How it was checked | Status |
|---|---|---|---|
| 1 | Shopper logs in with correct password | Login API, and the UI demo chip | Checked |
| 2 | Wrong password is refused | Login API | Not yet run |
| 3 | Sale price is applied at add to cart | Laptop 55000 at 15% = 46750 (ass8 and ass9) | Checked |
| 4 | Same product again raises quantity | Unit test | Automated |
| 5 | Checkout creates an order and empties the cart | Checkout via curl; order in history (ass8 and ass9) | Checked |
| 6 | Anonymous shopper is refused | Add to cart and checkout without a token return 401 | Checked |
| 7 | Checkout fails when stock is short and stock is restored | Webcam at 0 stock returned 409; Laptop stock and cart unchanged | Checked |
| 8 | Non-admin cannot create, edit, or read admin data | Shopper gets 403 on product writes and `/api/orders/stats` | Checked |
| 9 | Admin changes sale % and shopper sees it | Admin edit, then a -25% badge on the shop card. Cart keeps the price it had | Checked (browser) |
| 10 | Category filter returns only that category | `?category=Audio` and `?category=Fashion` | Checked |
| 11 | Same product in two sizes makes two cart lines and two order lines | Running Shoes size 9 and 10 (ass9); size 8 twice merged into one line of quantity 2 (ass8) | Checked |
| 12 | Size and colour survive to the order | Order lines show size and colour (ass9 and ass8) | Checked |
| 13 | Invalid PIN is refused at checkout | PIN of 2 digits returns 400 | Checked |
| 14 | Coupon and delivery fee are applied | SAVE10 on 999 mat with 5% sale plus 99 delivery = 953.14 total, GST 145.39 included | Checked |
| 15 | Review is saved on the product | Review posted, the review count grows, and the reviewer's name is shown | Checked |
| 16 | Shopper's orders are linked to their own account | `shopper_asha` sees her 18 seeded orders | Checked |
| 17 | Admin dashboard numbers come from the orders | Stats: revenue, count, average, 14 daily points, top products | Checked |
| 18 | Admin status change moves an order along | Status update via API | Not yet run |
| 19 | Photo credits page and footer link | Built; not yet checked in a browser | Not yet run |
| 20 | Wishlist, compare, recently viewed, budget filter, coupon box | Built; click-through in the browser | Not yet run |

---

## 12. Ease of use and coding practices

**For users**
- Sign in, browse, buy, and see the invoice without a manual.
- Demo buttons on the login page for each role.
- One visual style across storefront, product page, orders, admin, and developer console.

**For developers**
- **Clear packages.** Controllers handle HTTP; services hold the rules; repositories talk to MongoDB.
- **Small, named shapes.** Records for lines, addresses, checkout requests, and reviews.
- **One auth check per service.** `AuthClient` (ass9) or `AuthService` (ass8).
- **Comments where a reader would be stuck,** such as why stock is checked before an order is saved.
- **Status codes that mean something.** 400 for bad input, 401 for no login, 403 for wrong role, 404, 409 for out of stock.
- **Failure handling in checkout.** Reserved stock is returned if a later line fails.
- **Money rounded at checkout** so totals match the invoice.
- **Seed data and scripts.** The demo data script rebuilds products, shoppers, reviews and order history on any machine.

---

## 13. How to run it

**Requirements:** JDK 21, Node 18+, MongoDB on `localhost:27017`, `mongosh`, and `python3`.

**Exercise 8 (monolith)**
1. `cd ass8/cart-backend && ./mvnw spring-boot:run` (port 8082)
2. `cd ass8/cart-frontend && npm install && npm run dev`
3. Load the demo data: `scripts/seed-demo-data.sh ass8`
4. Open the URL Vite prints and sign in.

**Exercise 9 (microservices)**
1. In four terminals, start `user-service`, `product-service`, `cart-service`, and `order-service` (each with `./mvnw spring-boot:run`).
2. `cd ass9/cart-frontend && npm install && npm run dev`
3. Load the demo data: `scripts/seed-demo-data.sh ass9`
4. Open the URL Vite prints and sign in.

**Demo accounts** (password for all: `demo123`): `shopper`, `shopper_asha`, `shopper_ravi`, `shopper_meera`, `shopper_kiran`, `shopper_divya`, `shopper_arjun`, `shopper_priya`, `shopper_sahil`, `admin`, `developer`.

**Product photos** come from `scripts/fetch-product-images.py`, which searches Wikimedia Commons and writes the photos and `credits.json` into both frontends. Run it again after adding a product that needs a photo, and add the product's search words to its list.

On macOS with Homebrew, set `JAVA_HOME` to JDK 21 before running Maven, for example `export JAVA_HOME=$(brew --prefix openjdk@21)`.

---

## 14. Known limits and next steps

- **Payments are simulated.** Checkout records the payment method and places the order. No payment provider is called.
- **Passwords use SHA-256 for demo purposes.** A real store would use bcrypt or argon2.
- **No API gateway.** The browser calls each service directly. A gateway would be the next step for production, along with load balancing.
- **Reviews are not limited to buyers.** Any logged-in shopper can review. "Verified purchase" is shown as text on every review and is not checked against orders yet.
- **Wishlist, recently viewed, compare and the budget filter live in this browser only** (localStorage). They do not follow the shopper to another device.
- **Coupon codes are a fixed list** (`SAVE10`, `WELCOME20`) in the Order service. The shop previews the same two codes.
- **Seeded orders are written straight into MongoDB** so they can carry past dates. Orders placed through the app are dated now.
- **Admin-added products need a photo** from the photo script. Until then the storefront shows a broken image for that product.
- **Developer logs are per service and live in each service's own database.** There is no central log store, which is the microservice trade-off the console shows.
- **No automated browser tests.** Cases 18 to 20 are manual for now.

---

## 15. Notes for future developers

- To add a product field: change the model, the create and update methods in the Product service, the admin form, and the product page. The storefront reads the same API.
- To add a coupon: add the code and its percentage to `COUPONS` in the Order service's `CheckoutService`, and to the same list in the shop view.
- To add a role: add it to the seed data, the route guard in `router/index.js`, and the admin check in the service.
- To add a service in ass9: copy `cart-service`'s pom, add its URL to the other services' `application.properties`, and add it to the Developer page's service list.
- Keep the API paths the same across ass8 and ass9 so the frontend stays shared. Copy shared views between the two frontends after editing.

---

## 16. Code map: where the key pieces live

Use this to find the code behind each idea in the deck. Paths are from the repo root.

### CORS (cross-origin requests)

The browser runs on Vite's dev port and calls each backend on another port, so every backend allows cross-origin calls. There is no global CORS config: each controller carries `@CrossOrigin(origins = "*")`.

- **ass8 (one backend):** `ass8/cart-backend/src/main/java/com/ssn/cartbackend/controller/` (`AuthController`, `CartController`, `ProductController`, `OrderController`, `DevController`).
- **ass9 (four services):** `ass9/user-service/.../controller/AuthController.java` and `DevController.java`; `ass9/product-service/.../controller/ProductController.java` and `DevController.java`; `ass9/cart-service/.../controller/CartController.java` and `DevController.java`; `ass9/order-service/.../controller/OrderController.java` and `DevController.java`.
- A path with no handler (such as a bare `GET /api`) gets no CORS header, so the browser cannot read its reply. The service map therefore probes `/dev/metrics` in `ass9/cart-frontend/src/api/client.js` (`ping`).

### HTTP calls between services (RestTemplate)

Only ass9 makes HTTP calls between services. ass8 calls its own repositories directly.

- **Bean definitions:** `ass9/cart-service/src/main/java/com/ssn/cartservice/config/AppConfig.java` and `ass9/order-service/src/main/java/com/ssn/orderservice/OrderServiceApplication.java`.
- **Cart to Product (price lookup):** `ass9/cart-service/.../service/CartService.java`, `fetchProduct()`, which calls `GET /api/products/{id}`. The same file's `requireStock()` enforces stock on add and on quantity increase.
- **Order to Cart and Product (checkout):** `ass9/order-service/.../service/CheckoutService.java`. It reads the cart (`GET /api/cart`), reserves stock per line (`POST /api/products/{id}/reserve`), releases it on failure (`/release`), and clears the cart (`DELETE /api/cart`).
- **Every service to User (who is calling):** `AuthClient.java` in each service's `security/` folder. It calls `GET /api/auth/me` with the caller's token, and the URL comes from `user.service.url` in `application.properties`.

### Pinia stores (frontend state)

Each store is defined with `defineStore` in `cart-frontend/src/stores/`. ass8 and ass9 have the same three.

- **`auth.js`:** who is signed in, and their role. The router guard and the header read it.
- **`cart.js`:** the cart lines, the count and the total. It has a guest path that keeps the cart in `localStorage` (`shopcart-guest-cart`) and a server path. `mergeGuest()` moves the guest cart to the server at sign-in.
- **`shopper.js`:** the shopper's saved items, recently viewed products, compare list and saved addresses. All of this lives in this browser.

Views read the stores directly. Components do not receive data through props.

### Props and emits (component communication)

The frontend has no `defineProps` or `defineEmits`, and no `$emit`. The one component, `ass9/cart-frontend/src/components/ServiceMap.vue`, reads its data from its own script. The views are full pages, so they share state through Pinia and the router, not through parent/child events.

### Routes and the router guard

`cart-frontend/src/router/index.js` (ass8 and ass9 are the same). Shoppers can browse without signing in. `meta.roles` restricts pages to a role, and `meta.shopper` sends staff back to their own home page. Sign-in is needed only at checkout.

### Where to find a feature

| Feature | ass8 | ass9 |
|---|---|---|
| Cart page and floating cart | `ass8/cart-frontend/src/views/Cart.vue`, `App.vue` | `ass9/cart-frontend/src/views/Cart.vue`, `App.vue` |
| Offers and coupons | `ass8/cart-frontend/src/views/Offers.vue` | `ass9/cart-frontend/src/views/Offers.vue` |
| Stock check on add-to-cart | `ass8/cart-backend/.../service/CartService.java` | `ass9/cart-service/.../service/CartService.java` |
| Coupon percentages | `ass8/cart-backend/.../service/CheckoutService.java` | `ass9/order-service/.../service/CheckoutService.java` (`COUPONS`) |
| Admin product table | `ass8/cart-frontend/src/views/Admin.vue` | `ass9/cart-frontend/src/views/Admin.vue` |
| Developer console | `ass8/cart-frontend/src/views/Developer.vue` (one log stream) | `ass9/cart-frontend/src/views/Developer.vue`, `components/ServiceMap.vue` |
| Demo shopper list on login | `ass8/cart-frontend/src/views/Login.vue` | `ass9/cart-frontend/src/views/Login.vue` |

---

## Demo data and what the script builds

`scripts/seed-demo-data.sh ass9` (or `ass8`) fills a running backend with:

- **24 products** across Electronics, Audio, Wearables, Photography, Gaming, Fashion, Home, Fitness, and Stationery. Each has a description, specs, sizes or colours where they apply, and 3 to 6 reviews from the shopper accounts.
- **8 shopper accounts** (`shopper_asha` and others), password `demo123`.
- **About 110 orders** over 60 days. Each has one of eight delivery addresses, a payment method, sometimes a coupon, a delivery fee, GST and an invoice number. These drive the revenue chart and the product performance table.

Orders are written straight into MongoDB so they can carry past dates. Products, shoppers, reviews and orders are created the same way on each run without duplicating products.

Product photos come from Wikimedia Commons (`scripts/fetch-product-images.py`). Each photo is under a Creative Commons or public-domain licence, and the Credits page lists the author, licence, and source page for each.
