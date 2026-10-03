# Adds demo products and shopper accounts through the running APIs.
# Usage: python3 seed-api.py <product-api-base> <user-api-base>
# Example (microservices): python3 seed-api.py http://localhost:8083/api http://localhost:8085/api
import json
import sys
import urllib.request

PRODUCT_API, USER_API = sys.argv[1], sys.argv[2]

PRODUCTS = [
    ("Tablet Pro", "Electronics", 32999, 14, 8), ("Smartphone X", "Electronics", 45999, 20, 10),
    ("Mirrorless Camera", "Photography", 58999, 6, 15), ("Wireless Earbuds", "Audio", 5499, 35, 22),
    ("Gaming Chair", "Gaming", 15999, 8, 12), ("Denim Jacket", "Fashion", 2799, 22, 0),
    ("Leather Wallet", "Fashion", 1199, 45, 18), ("Coffee Grinder", "Home", 3499, 18, 0),
    ("Desk Lamp", "Home", 1899, 27, 15), ("Yoga Mat", "Fitness", 999, 50, 5),
    ("Dumbbell Set", "Fitness", 4999, 10, 20), ("Notebook Set", "Stationery", 499, 80, 0),
]
SHOPPERS = ["asha", "ravi", "meera", "kiran", "divya", "arjun", "priya", "sahil"]


def post(url, body, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=json.dumps(body).encode(), method="POST", headers=headers)
    with urllib.request.urlopen(req) as res:
        return json.load(res)


token = post(USER_API + "/auth/login", {"username": "admin", "password": "demo123"})["token"]
with urllib.request.urlopen(PRODUCT_API + "/products") as res:
    existing = {p["name"] for p in json.load(res)}
for name, category, price, qty, discount in PRODUCTS:
    if name in existing:
        print("product already exists:", name)
        continue
    post(PRODUCT_API + "/products", {"name": name, "category": category, "price": price,
                                     "quantity": qty, "discountPercent": discount}, token)
    print("product", name)
for shopper in SHOPPERS:
    try:
        post(USER_API + "/auth/register", {"username": "shopper_" + shopper, "password": "demo123"})
        print("shopper", shopper)
    except urllib.error.HTTPError:
        print("shopper already exists:", shopper)
