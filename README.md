# MinuteFlow

*MinuteFlow* is a small *spring-boot* library, which allows to easily implement the workflow.

*MinuteFlow* have three unique features, which make it a good choice
for implementation of the embedded workflow in micro-services:
1. the workflow is executed on top of the application entities
2. the workflow is composed of the application services
3. the workflow can use calculated states  
   (the calculated state is a special state, which is applied based on some condition)

*MinuteFlow* is using:
1. Java 17
2. Spring-Boot 3.0.13

For more info please check project [wiki](https://github.com/jan-komrska/minuteflow/wiki)
or two examples in *test-application* module in the repository.

Maven dependency [central](https://repo1.maven.org/maven2/org/minuteflow/minuteflow-core/0.3.6/):

```
<dependency>
  <groupId>org.minuteflow</groupId>
  <artifactId>minuteflow-core</artifactId>
  <version>0.3.6</version>
</dependency>
```
