#!/bin/bash

SALT=`tr -dc A-Za-z0-9 </dev/urandom | head -c 32 ; echo ''`

echo -n "enter your password: "
read -s PASSWORD
echo ""
echo -n "re-enter your password: "
read -s PASSWORD_CONFIRM
echo ""

if [[ "$PASSWORD" != "$PASSWORD_CONFIRM" ]];
then
  echo "passwords don't match"
  exit 1
fi

OBFUSCATED_PASSWORD=`echo -n "$SALT:$PASSWORD" | base64`

echo "obfuscated password: BASE64:$OBFUSCATED_PASSWORD"
