#!/bin/bash

set -e

source version.env

rm -Rf temp
mkdir -p temp

rm -Rf target
mkdir -p target

cp simpleclient/target/simpleclient*-jar-with-dependencies.jar temp/simpleclient.pkg
cp exporter/target/exporter*-jar-with-dependencies.jar temp/exporter.pkg
cp javaagent/target/javaagent*-jar-with-dependencies.jar temp/javaagent-${VERSION}.jar
cp configuration/exporter.yml temp/.
cp configuration/localhost.pkcs12 temp/.
cp temp/javaagent-${VERSION}.jar temp/metrics-exporter-javaagent-${VERSION}.jar
cp generate-obfuscated-password.sh temp/.
cp generate-hashed-password.sh temp/.

zip -q -j -9 temp/metrics-exporter-javaagent-${VERSION}.jar temp/exporter.pkg
zip -q -j -9 temp/metrics-exporter-javaagent-${VERSION}.jar temp/simpleclient.pkg

cp temp/metrics-exporter-javaagent-${VERSION}.jar target/.

echo ""
echo "Final javaagent jar..."
echo ""
echo "    target/metrics-exporter-javaaagent-${VERSION}.jar"
echo ""

cp standalone/target/standalone-${VERSION}-jar-with-dependencies.jar target/metrics-exporter-standalone-${VERSION}.jar

echo "Final standalone jar..."
echo ""
echo "    target/metrics-exporter-standalone-${VERSION}.jar"
echo ""

zip -q -j -9 temp/metrics-exporter-javaagent-${VERSION}.zip temp/metrics-exporter-javaagent-${VERSION}.jar temp/exporter.yml temp/generate-obfuscated-password.sh temp/generate-hashed-password.sh

cp temp/metrics-exporter-javaagent-${VERSION}.zip target/.

echo "Zip package..."
echo ""
echo "    target/metrics-exporter-javaagent-${VERSION}.zip"
echo ""

tar -cf temp/metrics-exporter-javaagent-${VERSION}.tar.gz -C temp metrics-exporter-javaagent-${VERSION}.jar exporter.yml generate-obfuscated-password.sh generate-hashed-password.sh --owner=0 --group=0

cp temp/metrics-exporter-javaagent-${VERSION}.tar.gz target/.

echo "tar.gz package..."
echo ""
echo "    target/metrics-exporter-javaagent-${VERSION}.tar.gz"
echo ""