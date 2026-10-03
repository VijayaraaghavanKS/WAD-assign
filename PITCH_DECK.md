# MaisonCart: Pitch Deck

A small online store built two ways for ICS1511 Web Application Development:

- **Exercise 8 (`ass8/`)**: one Spring Boot app, one database, one port. A monolith.
- **Exercise 9 (`ass9/`)**: four separate Spring Boot services, each with its own database and port. Microservices.

Both use the same Vue 3 storefront, the same API paths, and the same demo accounts. You can compare the two designs side by side.

---

## 1. The idea in one minute

Shoppers browse a catalogue, add items to a cart, and check out. Admins manage stock and prices. Developers watch what the system is doing in real time.

The point is not to compete with Amazon on scale. The point is to show, clearly and in working code, how a real store is put together: login and roles, stock that is reserved safely at checkout, sale prices that are fixed at the moment of purchase, and a console that makes the system easy to inspect.

---

## 2. Two versions, one storefront

| | Exercise 8 (monolith) | Exercise 9 (microservices) |
|---|---|---|
| Backend | 1 Spring Boot app | 4 Spring Boot apps |
| Port | 8082 | 8083 (product), 8084 (cart), 8085 (user), 8086 (order) |
| Database | `ShoppingCartDB` | One database per service |
| Calls between parts | Direct method calls | HTTP calls over REST |
| Frontend | Same code, all calls go to port 8082 | Same code, calls go to each service's port |
| Good for | Simple deployment, fast local runs | Scaling and deploying parts separately |

The frontend is nearly identical in both. Only the base URLs differ, so the same pages work against either backend.

---

## 3. Architecture

**Exercise 9 (microservices):**

```
                 Browser (Vue 3 + Pinia)
     /login    /        /orders     /admin      /dev
        |        |          |           |          |
        +--------+----------+-----------+----------+
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
                  Cart Service and Order Service also ask
                  User Service "who owns this token?"
```

**Exercise 8 (monolith):**

```
Browser (Vue 3 + Pinia)  --->  Spring Boot app :8082
                                 |- user package    (login, tokens)
                                 |- product package (catalogue, stock)
                                 |- cart package    (per-user carts)
                                 |- order package   (checkout, history)
                                 |- dev package     (request log, metrics)
                                 |
                              ShoppingCartDB (MongoDB)
```

---

## 4. Services and what each one owns

| Service | Owns | Does not own |
|---|---|---|
| **User** (ass9 only) | Accounts, password hashes, login tokens, roles | Anything about products or carts |
| **Product** | Catalogue, categories, prices, sale %, stock | Who is buying |
| **Cart** | Each user's cart lines, sale price copied at add time | Stock levels (checked at checkout) |
| **Order** | Placed orders, order history, the checkout steps | Product or user records (copied in or looked up) |

In the monolith (ass8), the same four responsibilities exist as packages inside one app.

---

## 5. API endpoints

Every endpoint that acts on a user needs `Authorization: Bearer <token>`. The token comes from `/api/auth/login`.

### User Service (ass9 `:8085`; in ass8 the same paths are on `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | Anyone | Creates a shopper account |
| POST | `/api/auth/login` | Anyone | Returns `{ token, username, role }` |
| GET | `/api/auth/me` | Any logged-in user | Returns who owns the token |

### Product Service (ass9 `:8083`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| GET | `/api/products` | Anyone | Lists products. Optional `?category=Audio` |
| GET | `/api/products/{id}` | Anyone | One product |
| POST | `/api/products` | Admin | Adds a product |
| PUT | `/api/products/{id}` | Admin | Edits name, price, stock, category, sale % |
| DELETE | `/api/products/{id}` | Admin | Removes a product |
| POST | `/api/products/{id}/reserve` | Order service (ass9 only) | Takes stock during checkout. Returns 409 if stock is short |
| POST | `/api/products/{id}/release` | Order service (ass9 only) | Puts stock back if checkout fails partway |

### Cart Service (ass9 `:8084`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| GET | `/api/cart` | Logged-in user | Their cart |
| POST | `/api/cart/add/{productId}` | Logged-in user | Adds one, or adds 1 to an existing line. Applies sale price |
| PUT | `/api/cart/{cartItemId}` | Logged-in user | Sets a quantity |
| DELETE | `/api/cart/{cartItemId}` | Logged-in user | Removes one line |
| DELETE | `/api/cart` | Logged-in user | Empties their cart |

### Order Service (ass9 `:8086`; ass8 `:8082`)

| Method | Path | Who | What it does |
|---|---|---|---|
| POST | `/api/orders` | Logged-in user | Checkout: takes stock, saves the order, empties the cart |
| GET | `/api/orders` | Logged-in user | Their order history, newest first |

### Developer endpoints (for the console)

| Method | Path | Services | What it shows |
|---|---|---|---|
| GET | `/api/dev/logs` | Product, Cart (ass9); one app (ass8) | The last 100 requests: method, path, status, time taken |
| GET | `/api/dev/metrics` | Product, Cart (ass9); one app (ass8) | Total requests, errors, average time, uptime |

---

## 6. End-to-end flow: a shopper buys a laptop

1. **Sign in.** The shopper enters their username and password, or clicks the "Shopper" demo chip. The User service checks the password hash and returns a token. The browser saves the token, so a refresh keeps them signed in.
2. **Browse.** The shop page loads products. Each card shows the sale price, the original price struck through, a discount badge, and stock warnings ("Only 3 left", "Out of stock").
3. **Filter.** The shopper clicks a category chip. The page asks the Product service for `?category=Electronics`.
4. **Add to cart.** The Cart service looks up the product price and sale %, works out the sale price, and saves the line with that price. If the item is already in the cart, only the quantity goes up.
5. **Adjust.** The cart panel updates quantities and removes lines. Totals are worked out from the saved prices.
6. **Checkout.** The Order service:
   - reads the cart,
   - takes stock for each line (if any line is short, it puts back the stock it already took and returns "Some items are out of stock"),
   - saves the order with a snapshot of names, prices and quantities,
   - empties the cart.
7. **Confirmation.** The shopper sees "Order placed" with a short reference. Order history on the "My orders" page shows the purchase.

**Under the hood in ass9:** step 4 is one HTTP call from Cart to Product. Step 6 is HTTP calls from Order to Cart (read), Product (reserve, release), and Cart (clear). Every service also checks the token with User.

---

## 7. Roles and what each one sees

| Role | Demo account | Can see | Can do |
|---|---|---|---|
| Shopper | `shopper` / `demo123` | Shop, My orders | Browse, cart, checkout |
| Admin | `admin` / `demo123` | Shop, Admin | Everything a shopper does, plus manage inventory |
| Developer | `developer` / `demo123` | Shop, Developer console | Watch logs and metrics, see service status |

Route guards in the browser hide pages a role should not open. The backend also checks roles on every write, so hiding a button is never the only protection.

---

## 8. Benefits for the admin

The Admin page is an inventory table, not a form that you have to fill in again and again.

- **Add a product** with name, category, price, stock, and sale % in one row.
- **Edit inline.** Click Edit and change name, category, price, sale %, or stock. Save or cancel.
- **See stock at a glance.** Out-of-stock and low-stock badges show what needs restocking.
- **Control prices and promotions.** Sale % is per product. The storefront shows the sale automatically and the cart keeps the price the shopper saw.
- **Remove products** that are discontinued.
- **No code changes or redeploys** are needed to add a product, change a price, or restock.
- **Stock cannot go negative.** Checkout checks stock and fails cleanly with a clear message if an item sold out just before the shopper paid.

---

## 9. Developer console and debugging

The Developer page is for anyone who has to understand or change the system later.

- **Live service map (ass9).** Shows the five parts of the system and whether each one answers. Dots turn green when a service responds and red when it does not. The check runs every 5 seconds.
- **Request logs.** The last 100 requests, with method, path, status, and time taken, across the services.
- **Metrics.** Total requests, error count, average response time, and uptime.
- **Endpoint reference.** A list of every API endpoint, which service it belongs to, and what it does, so nobody needs to read controller code to find a route.
- **Clear error messages.** Every failure returns a status code and a readable message ("Log in first", "Admins only", "Some items are out of stock"). The browser shows the message in a toast.

**Where to look when something breaks:**
- Login fails: check the User service on `:8085`, then the `users` collection.
- Price looks wrong: check the sale % on the product, then the cart line (the price is copied when the item is added).
- Checkout fails: the message says whether the cart was empty or stock ran out. Stock is put back automatically if checkout stops partway.
- Something slow: the Developer page shows average time per request and the recent request list.

---

## 10. What makes this different from a typical student store

| Typical student e-commerce project | MaisonCart |
|---|---|
| Anyone can change anything | Roles on every API write, not just in the UI |
| Price changes rewrite old carts and orders | Sale price is copied when the item is added, and orders keep a snapshot |
| Stock goes negative or is never checked | Stock is reserved at checkout and returned if checkout fails partway |
| Teacher has to read the code to see what happens | Developer console shows the live service map, requests, and metrics |
| One design for everything | Same storefront works against a monolith or microservices |
| Login is decorative | Real tokens, demo accounts for each role, persisted session |

**Compared with large marketplaces:** we do not match their scale, recommendations, payments or delivery network. We do show the same core ideas (sessions, roles, stock safety, order history) in a form a developer can read end to end.

---

## 11. Testing

### Automated tests (run with `./mvnw test`)

| Test | Service | What it checks |
|---|---|---|
| `ProductControllerTest` (4 tests) | Product (ass9) | Listing products, getting one product, and that creating a product needs an admin token |
| `CartServiceTest` (2 tests) | Cart (ass9) | A new product is added with its sale price applied. Adding an existing product only increases the quantity |
| `CartServiceTest` (2 tests) | ass8 | Same two cart rules, inside the monolith |

### Real-life use cases

| # | Scenario | How it was checked | Status |
|---|---|---|---|
| 1 | Shopper logs in with correct password | Login API via curl, and UI with demo chip | Checked |
| 2 | Wrong password is refused | Login API | Not yet run |
| 3 | Shopper adds a sale item; price is discounted | Add-to-cart via curl (Laptop 55000 at 15% off = 46750) | Checked (ass8 and ass9) |
| 4 | Adding the same item twice increases quantity | Unit test | Automated |
| 5 | Shopper checks out and gets an order | Checkout via curl; order appears in history | Checked (ass8 and ass9) |
| 6 | Shopper is refused without a token | Anonymous add returns 401 | Checked (ass8 and ass9) |
| 7 | Checkout fails when stock is short and stock is restored | Curl: Webcam at 0 stock, checkout returned 409, Laptop stock unchanged, cart kept | Checked |
| 8 | Non-admin cannot create or edit products | Curl: shopper gets 403 on create and edit, no token gets 401. Admin edit saved in UI | Checked |
| 9 | Admin changes sale % and shopper sees it | Admin UI edit, then shop card showed -25% badge. Cart kept the old price, as designed | Checked (browser) |
| 10 | Category filter shows only that category | Curl: `?category=Audio` and `?category=Fashion` return only matching products | Checked |

---

## 12. Ease of use and coding practices

**For users**
- Sign in, browse, buy, and see history without a manual.
- Demo buttons on the login page for each role, so a tutor can try every role in seconds.
- One consistent visual style across the storefront, admin, and developer pages.

**For developers**
- **Clear package layout.** Controllers only handle HTTP. Services hold the rules. Repositories talk to MongoDB.
- **Small, named pieces.** Records for request and line shapes. One `AuthClient` (ass9) or `AuthService` (ass8) for every token check.
- **Comments only where a reader would be stuck.** For example, why a stock check happens before saving an order.
- **Errors use status codes.** Callers can tell "not found" from "forbidden" from "out of stock".
- **Failure handling in checkout.** Stock is put back if checkout stops partway, so a failed order does not lose stock.
- **Sensible defaults.** Ports, database names, and service URLs are in each `application.properties`. Nothing is hard-coded across services.
- **Seed data on first start.** Products and demo accounts are created automatically on an empty database.

---

## 13. How to run it

**Requirements:** JDK 21, Node 18+, MongoDB on `localhost:27017`.

**Exercise 8 (monolith)**
1. `cd ass8/cart-backend && ./mvnw spring-boot:run` (port 8082)
2. `cd ass8/cart-frontend && npm install && npm run dev`
3. Open the URL Vite prints and sign in with a demo account.

**Exercise 9 (microservices)**
1. Start each service in its own terminal: `user-service`, `product-service`, `cart-service`, `order-service` (each with `./mvnw spring-boot:run`).
2. `cd ass9/cart-frontend && npm install && npm run dev`
3. Open the URL Vite prints and sign in with a demo account.

**Demo accounts** (password for all: `demo123`): `shopper`, `admin`, `developer`.

On macOS with Homebrew, set `JAVA_HOME` to JDK 21 before running Maven, for example `export JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home`.

---

## 14. Known limits and next steps

- **Payments are simulated.** Checkout places the order without a payment provider.
- **Passwords use SHA-256 for demo purposes.** A real store would use bcrypt or argon2.
- **No API gateway.** The browser talks to each service directly. A gateway would be the next step for a production deployment, along with load balancing.
- **Developer logs cover product and cart only (ass9).** The User and Order services do not expose logs yet.
- **No automated end-to-end browser tests.** Use case 7 to 10 are manual for now.

---

## 15. Notes for future developers

- To add a product field, change the model, the controller's update method, and the admin form. The storefront reads from the same API.
- To add a new role, add it to the seed data, the route guard in `router/index.js`, and the `requireAdmin`-style check in the service.
- To add a new service in ass9, copy `cart-service`'s pom, add its URL to the other services' `application.properties`, and add a row to the endpoint table in the Developer page.
- Keep the API paths the same across ass8 and ass9 so the frontend stays shared.
