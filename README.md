# Solace simple client

A simple client for Solace.

## Overview

Client to connect with Solace using _publish-subscribe_ or _request-reply_ patterns.

## Getting started

It is necessary to have [Apache Maven] installed.

1. Compile the project using Maven: `mvn package`
1. Execute the generated .jar with dependencies: `java -jar target/solace-client-jar-with-dependencies.jar`

## Additional configuration

### Passing params in one line

Option can be passed on the same line the program is executed as follows:

```bash
java -jar target/solace-client-jar-with-dependencies.jar 0    # JCSMP is chosen
java -jar target/solace-client-jar-with-dependencies.jar 0 c  # JCSMP consumer is chosen
java -jar target/solace-client-jar-with-dependencies.jar 1 0  # Request/Reply requestor is chosen
```

### Overwriting properties

Properties can be overwritten using the `-D` as follows:

```bash
java -jar \
    -Dsolace.topic_name='other.topic' \
    -Dsolace.host='tcps://my.custom.domain:55443' \
    -Dsolace.ssl.truststore='/path/to/my/truststore.jks' \
    -Dsolace.ssl.truststore.password='my-secure-pass' \
    target/solace-client-jar-with-dependencies.jar
```

## References

The examples are based on Solace documentation:

- [Solace Request/Reply]
- [Solace Publish/Subscribe]

[Apache Maven]: https://maven.apache.org/
[Solace Request/Reply]: https://solace.com/samples/solace-samples-java/request-reply/
[Solace Publish/Subscribe]: https://solace.com/samples/solace-samples-java/publish-subscribe/
