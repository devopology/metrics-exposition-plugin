# metrics-exporter

Project to expose JMX and / or Prometheus metrics in a Prometheus or OpenMetrics format via HTTP/HTTPS.

Can be used as either a Java agent (`-javaagnet`) or as a standalone application (EXPIREMENTAL) to remotely collect JMX metriccs.

## Features

- 100% pure Java compatibility
  - [https://www.oracle.com/java/technologies/faq-sun-packages.html](https://www.oracle.com/java/technologies/faq-sun-packages.html)
- HTTP/2 support
- SSL/TLS support
  - obfuscated keystore password configuration (no clear-text)
- HTTP BASIC authentication support
  - single user with a username / password
  - hashed user password configuration (no clear-text)
- server side metric caching support
  - server controlled collection interval
- fine-grained HotSport metrics configuration
- isolated exporter code from application code
- modern HTTP server
  - Uses Undertow 2.2.x
- Uses Prometheus `client_java` code
  - compatibility
- Uses Prometheus `jmx_exporter` code
  - compatibility

## Constraints

- requires Java 8+ (No Java 6 support)
- no runtime configuration reloading
- needs more testing

## Supported modes

### Java agent

- most common usage
- see `./run-test-application.sh`

### Standalone JMX exporter

- experimental
- see `./run-standalone.sh`

## Potential future features

- runtime configuration reloading
- pluggable authentication via an external jar
- support more Undertow configuration values
- better BASIC authentication password hashing format

## FAQ

  - [https://github.com/devopology/metrics-exporter/blob/main/FAQ.md](https://github.com/devopology/metrics-exporter/blob/main/FAQ.md)

## Typical Linux installation

There are two package formats depending on whether you want to use unzip or tar

- `metrics-exporter-javaagent-x.y.z.tar.gz`
- `metrics-exporter-javaagent-x.y.z.zip`

1. Create an installation directory:

```
mkdir /opt/metrics-exporter
```

2. Download / copy the installation package to your installation directory

```
/opt/metrics-exporter/metrics-exporter-javagent-x.y.z.zip
```

or

```
/opt/metrics-exporter/metrics-exporter-javaagent-x.y.z.tar.gz
```

3. Extract the installation package

```
cd /opt/metrics-exporter
unzip metrics-exporter-javagent-x.y.z.zip
```

or

```
cd /opt/metrics-exporter
tar -xvf metrics-exporter-javagent-x.y.z.tar.gz
```

4. Edit the `exporter.yml` configuration file

- see below for BASIC authentication password format
- the default username / password defined `exporter.yaml` is `test` / `password` 

5. Edit the `exporter.yml` configuration file, add your Prometheus `jmx-exporter` configuration

- https://github.com/prometheus/jmx_exporter
- https://github.com/prometheus/jmx_exporter/tree/master/example_configs

__NOTES:__

- A document separator (`---'` or `...`) can only exist on line 1 
- Prometheus web server configuration is ignored

6. Add Java javaAgentArguments to your application

__Example:__

```
java -javaagent:/opt/metrics-exporter/metrics-exporter-javaagent-x.y.z.jar=/opt/metrics-exporter/exporter.yml -jar <application jar>
```

__NOTES:__

- `/opt/metrics-exporter` should __NOT__ be in your classpath
- if your application bundles the `simpleclient` library, your application's version will be used
- values in the `exporter.yml` configuration file need to be edited to match your paths, ports, etc.

## Configuration

Configuration in `exporter.yml` is fairly self-explanatory.

- an example file is in `./configuration/exporter.yml`

__NOTES:__

- configuration is not guaranteed to be compatible with older major version (`1.y.z`, `2.y.z`)

### BASIC authentication configuration

The password required for BASIC authentication is in a salted hashed format.

Use the BASH script `./generate-hasheed-password.sh` to generate a salted hashed password (configuration value)

__NOTES__

- `sha512sum` is required

### Keystore password configuration

The password required for the keystore is in an obfuscated format.

Use the BASH script `./generate-obfuscated-password.sh` to generate an obfuscated password (configuration value)

## Build

Requires Java 8+ and Maven to build
Required Maven to build
Requires `zip` and `tar` to package

Clone
```
git clone https://github.com/devopology/metrics-exporter
```

Build
```
mvn clean package
```

Package
```
./package.sh
```

Packaging produces three artifacts

javaagent jar

- `./target/metrics-exporter-javaaagent-x.y.z.jar`

installation packages

- `./target/metrics-exporter-javaagent-x.y.z.tar.gz`
- `./target/metrics-exporter-javaagent-x.y.z.zip`

## Test application

Build
```
mvn clean package
```

Package
```
./package.sh
```

Run
```
./run.sh
```

Access the metrics-exporter

```
https://localhost:12345
```

- You should see HotSpot metrics
- You should see a single gauge named `fake`

__NOTES__

- a self-signed certificate is used - you will need to accept it
- username is `test`
- password is `password`

## Contributing

__PRs__

- fork the repository
- create a branch off of the `main` branch
- make your changes, test, etc.
- squash your commits
- create a PR targeting the upstream `main` branch

## References

- [https://github.com/prometheus/client_java](https://github.com/prometheus/client_java)
- [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## Notice

- Prometheus and associated projects (`simpleclient` and `jmx_exporter`) are © Prometheus Authors 2014-2022


- This project is __not__ associated, supported, or endorsed by the Prometheus project.
