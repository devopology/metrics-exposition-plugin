#!/bin/bash

SIMPLE_CLIENT_VERSION=0.16.0
VERSION=1.0.1

java -javaagent:javaagent/target/javaagent-${VERSION}.jar="exporter/target/exporter-${VERSION}.jar&simpleclient/target/simpleclient-${SIMPLE_CLIENT_VERSION}.jar&test-application/configuration/exporter.yml" -cp test-application/target/test-application-${VERSION}.jar TestApplication
