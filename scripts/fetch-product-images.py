# Downloads one open-licensed photo per product from Wikimedia Commons and writes
# credits.json next to them. Photos are CC0 / CC BY / CC BY-SA, so credit is required
# and is shown on the site's Credits page.
# Usage: python3 scripts/fetch-product-images.py   (writes into both frontends)
import html
import json
import os
import re
import time
import urllib.parse
import urllib.request

API = "https://commons.wikimedia.org/w/api.php"
HEADERS = {"User-Agent": "MaisonCart-college-project/1.0 (demo shop)"}
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TARGETS = [os.path.join(ROOT, "ass9/cart-frontend/public/products"),
           os.path.join(ROOT, "ass8/cart-frontend/public/products")]

# Product name -> (search words, words the photo's file name must contain, words it must not contain).
# The title check keeps wrong-but-related results (a camera for "mouse", say) out.
QUERIES = {
    "Laptop": ("laptop computer on desk", ["laptop", "notebook computer", "macbook"]),
    "Headphones": ("over-ear headphones", ["headphone"]),
    "Keyboard": ("computer keyboard", ["keyboard"], ["camera"]),
    "Mouse": ("wireless computer mouse", ["mouse"], ["chip", "microscope"]),
    "Webcam": ("webcam", ["webcam", "web cam"]),
    "Smartphone": ("smartphone", ["smartphone", "iphone"]),
    "Smartwatch": ("smartwatch", ["smartwatch", "smart watch"]),
    "Running Shoes": ("running shoes", ["running shoe", "sneaker", "trainer"]),
    "Travel Backpack": ("backpack", ["backpack", "rucksack"]),
    "Bluetooth Speaker": ("bluetooth speaker", ["speaker"]),
    "4K Monitor": ("computer monitor", ["monitor"]),
    "Tablet": ("tablet computer", ["tablet"]),
    "Tablet Pro": ("ipad", ["ipad"]),
    "Smartphone X": ("android phone", ["android", "phone"]),
    "Mirrorless Camera": ("mirrorless camera", ["camera"]),
    "Wireless Earbuds": ("wireless earbuds", ["earbud", "airpod", "earphone"]),
    "Gaming Chair": ("gaming chair", ["chair"]),
    "Denim Jacket": ("denim jacket", ["denim jacket", "jean jacket"]),
    "Leather Wallet": ("leather wallet", ["wallet"]),
    "Coffee Grinder": ("coffee grinder", ["grinder", "coffee mill"]),
    "Desk Lamp": ("desk lamp", ["desk lamp", "lamp"]),
    "Yoga Mat": ("yoga mat", ["yoga mat"]),
    "Dumbbell Set": ("dumbbells", ["dumbbell"]),
    "Notebook Set": ("notebooks", ["notebook"]),
}


def get(url):
    # Commons asks clients to slow down; back off and retry on 429.
    for attempt in range(6):
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req) as res:
                return res.read()
        except urllib.error.HTTPError as e:
            if e.code != 429:
                raise
            time.sleep(2 ** attempt)
    raise RuntimeError("Commons kept rate-limiting: " + url)


def slug(name):
    return re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")


def plain(text):
    return html.unescape(re.sub(r"<[^>]+>", "", text or "")).strip()


def find_photo(query, words, banned, used):
    params = {"action": "query", "generator": "search", "gsrsearch": "filetype:bitmap " + query,
              "gsrnamespace": 6, "gsrlimit": 20, "prop": "imageinfo", "iiprop": "url|extmetadata",
              "iiurlwidth": 800, "format": "json"}
    data = json.loads(get(API + "?" + urllib.parse.urlencode(params)))
    for page in sorted(data.get("query", {}).get("pages", {}).values(), key=lambda p: p.get("index", 0)):
        title = page["title"].removeprefix("File:")
        info = (page.get("imageinfo") or [None])[0]
        if not info or "thumburl" not in info or title in used:
            continue
        if not any(w in title.lower() for w in words) or any(w in title.lower() for w in banned):
            continue
        meta = info.get("extmetadata", {})
        return {
            "title": title,
            "thumb": info["thumburl"],
            "page": info["descriptionurl"],
            "author": plain(meta.get("Artist", {}).get("value", "Unknown")),
            "license": plain(meta.get("LicenseShortName", {}).get("value", "See page")),
            "licenseUrl": meta.get("LicenseUrl", {}).get("value", ""),
        }
    return None


def main():
    credits = {}
    used = set()
    for name, entry in QUERIES.items():
        query, words = entry[0], entry[1]
        banned = entry[2] if len(entry) > 2 else []
        time.sleep(1.5)
        photo = find_photo(query, words, banned, used)
        if photo is None:
            print("no photo for", name)
            continue
        time.sleep(1.5)
        image = get(photo["thumb"])
        for target in TARGETS:
            os.makedirs(target, exist_ok=True)
            with open(os.path.join(target, slug(name) + ".jpg"), "wb") as f:
                f.write(image)
        used.add(photo["title"])
        credits[name] = {k: v for k, v in photo.items() if k != "thumb"}
        print("ok", name, "-", photo["license"], "-", photo["author"][:40])
    for target in TARGETS:
        with open(os.path.join(target, "credits.json"), "w") as f:
            json.dump(credits, f, indent=2, ensure_ascii=False)


if __name__ == "__main__":
    main()
