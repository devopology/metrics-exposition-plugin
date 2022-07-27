# metrics-exporter

Project to expose JMX and / or Prometheus metrics in a Prometheus or OpenMetrics format via HTTP/HTTPS.

## Features

- 100% Java compliant
  - [https://www.oracle.com/java/technologies/faq-sun-packages.html](https://www.oracle.com/java/technologies/faq-sun-packages.html)
- HTTP/2 support
- SSL/TLS support (optional)
- HTTP BASIC authentication (optional)
  - single user with a username / password
  - password configuration stored in a salted format
- server side metrics caching (optional)
  - server controlled
- fine-grained HotSpot exports
  - configurable
- Isolates exporter code from application code
- Uses Undertow 2.2.x
- Uses Prometheus `client_java` code for compatibility
- Uses Prometheus `jmx_exporter` code for compatibility

## Potential future features

- pluggable authentication via an external jar
- support more Undertow configuration values

## Constraints

- Requires Java 8+ (No Java 6 support)
- Requires three jars
  - `javaagent-x.y.z.jar`
  - `exporter-x.y.z.jar`
  - `simpleclient-x.y.z.jar`
- Requires two configuration files
  - `exporter.yml`
  - `jmx-exporter.yml`
- Needs more testing

## Typical Linux installation

There are two package formats depending on whether you want to use unzip or tar

- `metrics-exporter.tar.gz`
- `metrics-exporter.zip`

1. Create an installation directory:

```
mkdir /opt/metrics-exporter
```

2. Copy the package to your installation directory

```
/opt/metrics-exporter/metrics-exporter.zip
```

or

```
/opt/metrics-exporter/metrics-exporter.tar.gz
```

3. Extract the package in your installation directory

```
cd /opt/metrics-exporter
unzip metrics-exporter.zip
```

or

```
cd /opt/metrics-exporter
tar -xvf metrics-exporter.tar.gz
```

4. Edit the `exporter.yml` configuration file

- configuration in `exporter.yml` is fairly self-explanatory
- see below for BASIC authentication password format
  - the default username / password in `exporter.yaml` is `test` / `password` 

5. Edit the `jmx-exporter.yml` configuration file

- https://github.com/prometheus/jmx_exporter
- https://github.com/prometheus/jmx_exporter/tree/master/example_configs

6. Add Java arguments to your application

__Example:__

```
java -javaagent:/opt/metrics-exporter/javaagent-x.y.z.jar="/opt/metrics-exporter/exporter-x.y.z.jar&/opt/metrics-exporter/simpleclient-x.y.z.jar&/opt/metrics-exporter/exporter.yml" -jar <application jar>
```

__NOTES:__

- javaagent argument order matters
- double quotes are required around the arguments
- `/opt/metrics-exporter` should __NOT__ be in your classpath
- if your application bundles the `simpleclient` library, your application's version will be used
- values in the `exporter.yml` configuration file need to be edited to match your paths, ports, etc.

## Configuration

Configuration in `exporter.yml` is fairly self-explanatory.

### BASIC authentication configuration

The password required for BASIC authentication is in a salted format.

Use the BASH script `./generate-salted-password.sh` to generate a salted password / configuration value

__NOTES__

- `sha1sum` is required

## Build

Requires Java 8+ and Maven to build

Required zip and tar to package

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

Packaging produces two different package formats

- `./target/metrics-exporter.zip`
- `./target/metrics-exporter.tar.gz`

## Test application

Build
```
mvn clean package
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

The project uses a modified Github Flow branching model

- branch `main` contains the latest release
- branch `develop` contains the next release

__PRs__

- fork the repository
- create a branch off of the `development` branch
- make your changes, test, etc.
- squash your commits
- create a PR targeting the upstream `development` branch

## Releases

Releases are created via a PR from the upstream `develop` branch to the upstream `main` branch

## References

- [https://github.com/prometheus/client_java](https://github.com/prometheus/client_java)
- [https://github.com/prometheus/jmx_exporter](https://github.com/prometheus/jmx_exporter)

## Notice

- Prometheus and associated projects (`simpleclient` and `jmx_exporter`) are © Prometheus Authors 2014-2022

- This project is __not__ associated, supported, or endorsed by the Prometheus project.
