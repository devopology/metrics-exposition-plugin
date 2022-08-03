# snakeyaml

Project to package a custom version of snakeyaml to work around a Java 8 issue.

Snakeyaml tries to instantiate a Logger...

https://bitbucket.org/snakeyaml/snakeyaml/src/49227c24d741dafe3b519a0d5f9d8413d803d9f3/src/main/java/org/yaml/snakeyaml/TypeDescription.java#lines-48

... which causes a `NullPointerException` because the Logger can't be instantiated.

As a workaround, the snakeyaml code was copied and the Logger code commented out.