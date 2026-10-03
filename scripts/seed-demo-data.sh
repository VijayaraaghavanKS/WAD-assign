#!/usr/bin/env bash
# Fills a running MaisonCart backend with demo data: products, shopper accounts,
# product pages with reviews, and about 110 orders spread over 60 days, so the
# admin charts and shopper order pages have something to show.
#
# Usage: scripts/seed-demo-data.sh ass9   (microservices; all four services running)
#        scripts/seed-demo-data.sh ass8   (monolith; the app on port 8082 running)
# Needs python3 and mongosh. Re-running is safe: orders from an earlier run are replaced.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
TARGET="${1:-}"

case "$TARGET" in
  ass9)
    PRODUCT_API=http://localhost:8083/api
    USER_API=http://localhost:8085/api
    DB_JSON='{"shop":"E-CommDB","users":"UserServiceDB","orders":"OrderServiceDB"}'
    ;;
  ass8)
    PRODUCT_API=http://localhost:8082/api
    USER_API=http://localhost:8082/api
    DB_JSON='{"shop":"ShoppingCartDB","users":"ShoppingCartDB","orders":"ShoppingCartDB"}'
    ;;
  *)
    echo "usage: $0 ass9|ass8" >&2
    exit 1
    ;;
esac

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

python3 "$HERE/seed/seed-api.py" "$PRODUCT_API" "$USER_API"

for file in seed-details seed-orders; do
  sed "s|__DB__|$DB_JSON|" "$HERE/seed/$file.js" > "$TMP/$file.js"
  mongosh --quiet "$TMP/$file.js"
done
