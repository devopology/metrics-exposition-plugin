# FAQ

<!-- ======================================================================= -->
## General
<!-- ======================================================================= -->

###### Why did you create this project when Prometheus has the `jmx-exporter` project?

- Major factors
  - 100% pure Java compatibility
    - [https://www.oracle.com/java/technologies/faq-sun-packages.html](https://www.oracle.com/java/technologies/faq-sun-packages.html)
  - HTTP/2 support
  - SSL/TLS support
    - obfuscate keystore password configuration (no clear-text)
  - HTTP BASIC authentication support
    - single user with a username / password
    - hashed password configuration (no clear-text)
  - server side metric caching support
    - server controlled collection interval
  - fine-grained HotSport metrics configuration
  - isolated exporter code from application code
  - modern HTTP server
    - Undertow 2.2.x
  - Uses Prometheus `client_java` code
    - compatibility
  - Uses Prometheus `jmx_exporter` code
    - compatibility


- ... and I like to code :)

###### Why don't you integrate these changes into the Prometheus `jmx-exporter` project?

- The Prometheus `simpleclient` and `jmx-exporter` are great projects!
  - I actually contribute to those projects
  - Core code from those project is used in this project 


- The Prometheus `jmx-exporter` project provides Java 6 support
  - This limits the choice of libraries
  - Most libraries require Java 8

###### Does the project support a "standalone" mode like the Prometheus `jmx-exporter` project?

- Yes
  - It's experimental
  - Feedback is appreciaated


###### Why is there no Java 6 support?

- Java 6 is really antiquated
  - There is only one company (Azul Systems) that I know of providing production level support


- Modern HTTP server libraries (such as Undertow, etc.) don't support Java 6

###### Won't the Java agent's use of external libraries cause an application dependency problem if I'm using different versions of the same libraries?

- No
  - The Java agent code has no external dependencies
  - The exporter code (which uses Undertow, etc) is isolated in custom child-first classloaders

###### Why is the jar so large? increased memory usage?

- The jars contain 3rd party code
  - Conceptually, unused classes could be removed using a tool such as ProGuard

- The custom child-first classloaders load the embedded "packages" (jars) into memory
  - Requirement for single jar packaging
  - Memory usage is fairly static after loading
  - Most of the memory usage comes from Undertow and the core Prometheus `client_java` and `jmx-exporter` code

###### What are the files `simpleclient.pkg` and `exporter.pkg` that I see listed when I inspect the Java agent jar?

- The Java agent code use custom child-first classloaders to load these "packages" (really jars)
  - I arbitrarily chose `simpleclient.pkg` and `exporter.pkg`

###### How is the latest version 3.y.z?

- Configuration format changes required incrementing the major version number


- Backward compatibility for such a specialized piece of code requires substantial work for little benefit

## Notice

- Prometheus and associated projects (`simpleclient` and `jmx_exporter`) are © Prometheus Authors 2014-2022

- This project is __not__ associated, supported, or endorsed by the Prometheus project.
