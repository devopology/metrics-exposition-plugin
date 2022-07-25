#!/bin/bash

echo "Packaging artifacts...";

echo "Removing ./target"
rm -Rf target

echo "Creating ./target"
mkdir -p target/temp

echo "Copying simpleclient/target/simpleclient*.jar -> target/temp/."
cp simpleclient/target/simpleclient*.jar target/temp/.

echo "Copying exporter/target/exporter*.jar -> target/temp/."
cp exporter/target/exporter*.jar target/temp/.

echo "Copying javaagent/target/javaagent*.jar -> target/temp/."
cp javaagent/target/javaagent*.jar target/temp/.

echo "Copying metrics-exporter/configuration/exporter.yml -> target/temp/."
cp configuration/exporter.yml target/temp/.

echo "Copying metrics-exporter/configuration/jmx-exporter.yml -> target/temp/."
cp configuration/jmx-exporter.yml target/temp/.

echo "Copying metrics-exporter/configuration/localhost.pkcs12 -> target/temp/."
cp configuration/localhost.pkcs12 target/temp/.

zip -j ./target/metrics-exporter.zip ./target/temp/*
echo "Artifact package ./target/metrics-exporter.zip"

tar cfvz ./target/metrics-exporter.tar.gz -C ./target/temp .
echo "Artifact package ./target/metrics-exporter.tar.gz"