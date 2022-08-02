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

SALTED_PASSWORD_SHA1=`echo -n "$SALT:$PASSWORD" | sha512sum | cut -d " " -f 1`

echo "hashed password: SHA512:$SALT:$SALTED_PASSWORD_SHA1"
