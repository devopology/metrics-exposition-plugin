#!/bin/bash

source version.env

java -jar standalone/target/standalone-${VERSION}-jar-with-dependencies.jar standalone/configuration/exporter.yml
