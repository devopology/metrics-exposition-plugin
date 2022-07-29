#!/bin/bash

source version.env

java -Dfoo=bar -javaagent:target/metrics-exporter-javaagent-${VERSION}.jar=test-application/configuration/exporter.yml -cp test-application/target/test-application-${VERSION}-jar-with-dependencies.jar TestApplication
