# Event4J

Event4J is a library designed to simplify message publishing to Kafka and REST endpoints with built-in support for error handling and retries. This library uses custom annotations to mark methods for Kafka and REST publishing and provides an annotation processor to handle the message publishing logic.

## Getting Started

### Prerequisites

- Java 17
- Maven 3.6+
- A running Kafka instance (for Kafka publishing)
- A PostgreSQL database (for error logging with REST publishing)

### Installation

Add the following dependency to your Maven `pom.xml` file:

```xml
<dependency>
    <groupId>org.event4j</groupId>
    <artifactId>event4j</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Configuration
Add the following properties to your application.properties file:
```properties
# Kafka Configuration
event4j.kafka.enable=false
event4j.kafka.topic=topic
event4j.kafka.retry-topic=retry-topic
event4j.kafka.error-topic=error-topic
event4j.kafka.retry-count=4
event4j.kafka.hosts=localhost:9092
event4j.kafka.retry-group-id=my-consumer-group

# REST Configuration
event4j.rest.enable=true
event4j.rest.url=http://localhost:8081/3
event4j.rest.error-table=error_table
event4j.rest.retry-count=4
event4j.rest.connection-url=jdbc:postgresql://localhost:5432/mydb
event4j.rest.connection-user=myuser
event4j.rest.connection-password=mypassword
```
### Usage
Create a Spring Boot application and import the Event4JAnnotationProcessor class:
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(Event4JAnnotationProcessor.class)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```
Create a REST controller and use the @KafkaPublisher and @RestPublisher annotations to mark methods for message publishing:
```java
package com.example.demo;

import org.event4j.kafka.KafkaPublisher;
import org.event4j.rest.RestPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @PostMapping("1")
    @KafkaPublisher
    public Person postConf1(@RequestBody Person person) {
        return person;
    }

    @PostMapping("2")
    @RestPublisher
    public Person postConf2(@RequestBody Person person) {
        return person;
    }
}
```
### License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.