#!/usr/bin/env bash
# Bash Administrator Bootstrap Script for GateNest

BASE_URL="${1:-http://localhost:8080/api/v1/auth/bootstrap-admin}"
BOOTSTRAP_TOKEN="${2:-bootstrap-gatenest-admin-secret-2026}"
FULL_NAME="${3:-System Administrator}"
EMAIL="${4:-admin@greenfield.com}"
MOBILE="${5:-+91 80 4912 0001}"
PASSWORD="${6:-admin1234}"

echo "========================================================="
echo " GateNest - Administrator Bootstrap Setup"
echo "========================================================="

echo "Connecting to GateNest backend at: $BASE_URL"

RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"bootstrapToken\": \"$BOOTSTRAP_TOKEN\",
    \"fullName\": \"$FULL_NAME\",
    \"email\": \"$EMAIL\",
    \"mobile\": \"$MOBILE\",
    \"password\": \"$PASSWORD\"
  }")

echo "Response from server:"
echo "$RESPONSE"
