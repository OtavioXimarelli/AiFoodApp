#!/bin/bash

# Test script for AiFoodApp authentication
echo "Testing AiFoodApp Authentication..."

# Base URL - change as needed
BASE_URL="https://aifoodapp.site"
#BASE_URL="http://localhost:8080"

# Test the auth endpoint
echo -e "\n\n==== Testing Authentication Status ===="
curl -s -X GET "${BASE_URL}/api/foods/test-auth" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  --cookie-jar cookies.txt \
  --cookie cookies.txt | jq .

# Attempt to create a food item
echo -e "\n\n==== Attempting to Create Food Item ===="
curl -s -X POST "${BASE_URL}/api/foods/create" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  --cookie-jar cookies.txt \
  --cookie cookies.txt \
  -d '{
    "name": "Test Apple",
    "quantity": "1 unit",
    "expirationDate": "2023-12-31"
  }' | jq .

# List food items
echo -e "\n\n==== Attempting to List Food Items ===="
curl -s -X GET "${BASE_URL}/api/foods" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  --cookie-jar cookies.txt \
  --cookie cookies.txt | jq .

echo -e "\n\nDone testing."
