# MinuteFlow

*MinuteFlow* is a small *spring-boot* library, which is using domain-driven design in order to implement  
the business logic (or the business workflow) on top of the domain entities.

*MinuteFlow* have two unique features, which make it a good choice
for implementation of the embedded workflow in micro-services:
1. the workflow is executed on top of the domain entities
2. the workflow is composed of the domain services

*MinuteFlow* is using:
1. Java 17
2. Spring-Boot 3.0.13

For more info please check project [wiki](https://github.com/jan-komrska/minuteflow/wiki)
or three examples in *test-application* module in the repository.

## Installation

*MinuteFlow* packages are stored in [maven central repository](https://repo1.maven.org/maven2/org/minuteflow/minuteflow-core/).  
For the latest version please use following dependency:

```
<dependency>
  <groupId>org.minuteflow</groupId>
  <artifactId>minuteflow-core</artifactId>
  <version>0.3.7</version>
</dependency>
```
