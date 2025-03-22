> :warning: **minuteflow library is replaced by [casecore library](https://github.com/jan-komrska/casecore)**

# MinuteFlow

*MinuteFlow* is a small *spring-boot* library, which is using domain-driven design in order to implement  
the business logic (or the business workflow) on top of the domain entities.

*MinuteFlow* adds object-oriented features to *spring-boot* (see [simple OOP example](https://github.com/jan-komrska/minuteflow/wiki/OOP-Example)).

*MinuteFlow* has two unique features, which make it a good choice for implementation  
of the embedded workflow in micro-services  (see [simple workflow example](https://github.com/jan-komrska/minuteflow/wiki/Workflow-Example)):
1. the workflow is executed on top of the domain entities
2. the workflow is composed of the domain services

*MinuteFlow* is using:
1. Java 17
2. Spring-Boot 3.0.13

For more info please check project [wiki](https://github.com/jan-komrska/minuteflow/wiki)
or examples in *test-application* module in the repository.

## Tutorial

Please check following basic examples:
- [simple OOP example](https://github.com/jan-komrska/minuteflow/wiki/OOP-Example)
- [simple workflow example](https://github.com/jan-komrska/minuteflow/wiki/Workflow-Example)
- [simple JPA encapsulation example](https://github.com/jan-komrska/minuteflow/wiki/JPA-Example)

Please check following advanced examples:
- [domain entity with lifecycle workflow](https://github.com/jan-komrska/minuteflow/wiki/Domain-Entity-Example)

## Installation

*MinuteFlow* packages are stored in [maven central repository](https://repo1.maven.org/maven2/org/minuteflow/minuteflow-core/).  
For the latest version please use following dependency:

```
<dependency>
  <groupId>org.minuteflow</groupId>
  <artifactId>minuteflow-core</artifactId>
  <version>0.4.2</version>
</dependency>
```
