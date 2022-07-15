# metrics-exposition-plugin

Project for use with the `pluggable-javaagent` to expose JMX and Prometheus metrics in a Prometheus or OpenMetrics format via HTTP/HTTPS.

[https://github.com/devoploogy/pluggable-javaagent](https://github.com/devopology/pluggable-javaagent)

## Features

- 100% Java compliant ([https://www.oracle.com/java/technologies/faq-sun-packages.html](https://www.oracle.com/java/technologies/faq-sun-packages.html))
- Uses Undertow 2.2.x
- Uses Prometheus `client_java` code
- Uses Prometheus `jmx_exporter` code
- Supports SSL/TLS
- Supports HTTP BASIC authentication

## Caveats

- Requires Java 8+ (No Java 6 support)
- Requires two jars (`pluggable-javaagent-1.0.0.jar` and `metrics-exposition-plugin-1.0.0.jar`)
- Requires two configuration files (examples, `metrics-exposition-plugin.properties` and `prometheus.yml`)
- Needs more testing (no JUnit or integration tests)

## Build

```
git clone https://github.com/devopology/metrics-exposition-plugin
mvn clean package
```

## Typical Linux installation (`/opt/metrics-exporter`)

```
mkdir /opt/metrics-exporter
```

copy `pluggable-javaagent-1.0.0.jar` (from the `pluggable-javaagent` project) to `/opt/metrics-exporter/`

copy `metrics-exposition-plugin-1.0.0.jar` to `/opt/metrics-exporter/`

create `/opt/metrics-exporter/metrics-exposition-plugin.properties`

__Example:__

```
# metrics-exposition-plugin.properties

# web server hostname to bind to
server.host=0.0.0.0

# web server port
server.port=1234

# web server basic authentication (default is false)
#server.authentication.basic.enabled=true

# web server basic authentication username
#server.authentication.basic.username=test

# web server basic authentication password
#server.authentication.basic.password=password

# web server SSL enabled (default is false)
#server.ssl.enabled=false

# web server SSL protocol (default is TLSv1.3)
#server.ssl.protocol=TLSv1.3

# web server SSL keystore filename
#server.ssl.keystore=<path>/<your keystore file>

# web server SSL keystore password
#server.ssl.keystore.password=<your keystore password>

# web server SSL keystore type (default is PKCS12)
#server.ssl.keystore.type=<your keystore type>

# web server SSL certificate alias
server.ssl.certificate.alias=<your certificate alias>

# Prometheus exporter configuration
prometheus.yaml=/opt/metrics-exporter/prometheus.yml

# Prometheus default exports (default is true)
#default.exporters.enabled=false
```

create `/opt/metrics-exporter/prometheus.yml` (Prometheus jmx-exporter YAML format)

- [https://github.com/prometheus/jmx_exporter/tree/master/example_configs](https://github.com/prometheus/jmx_exporter/tree/master/example_configs)

__Example:__

```
rules:
- pattern: ".*"
```

add `java` command line arguments

__Example:__

```
java -javaagent:/opt/metrics-exporter/pluggable-javaagent-1.0.0.jar="plugin.jar=/opt/metrics-exporter/metrics-exposition-plugin-1.0.0.jar&plugin.classname=MetricsExpositionPlugin&plugin.properties=/opt/metrics-exporter/metrics-exposition-plugin.properties" -jar <application jar>
```

__NOTES:__

- `metrics-exposition-plugin-1.0.0.jar` should __NOT__ be in your application's classpath.

## Future features

- use obfuscated `server.authentication.basic.password` value
- pluggable authentication
- support more Undertow configuration values

## References


- [https://github.com/devopology/pluggable-javaagent](https://github.com/devopology/pluggable-javaagent)
- [https://undertow.io/](https://undertow.io/)
- [https://github.com/prometheus/client_java](https://github.com/prometheus/client_java)
- [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## Notice

- This project is __not__ associated, supported, or endorsed by the Prometheus project.