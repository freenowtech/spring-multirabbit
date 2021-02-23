# Spring MultiRabbit
**Spring MultiRabbit** is a library to enable multiple RabbitMQ brokers in SpringBoot applications. The modules are:
* **spring-multirabbit-lib** - the main module, that provides the auto-configuration feature;
* **spring-multirabbit-lib-integration** - a module to test integration with Spring;
* **spring-multirabbit-example-java** - an example project in Java;
* **spring-multirabbit-example-kotlin** - an example project in Kotlin;
* **spring-multirabbit-extension-example** - an example project of how to extend spring-multirabbit;

## How-to
To use the library, the project must:
1. Be a SpringBoot project annotated with **@EnableRabbit**, as usual;
2. Import the library **spring-multirabbit-lib**;
3. Provide configuration for additional brokers in the new path **spring.multirabbitmq.connections**. All 
   attributes available for **spring.rabbitmq** can be used in **spring.multirabbitmq.connections**. 
4. Change the container factory context when using non-default connections:
   1. For ```RabbitTemplate```, use ```SimpleResourceHolder.bind()``` and ```SimpleResourceHolder.unbind()```;
   2. For ```@RabbitListener```, define the ```containerFactory``` or leave it blank for the default connection.

##### 1. Main SpringBoot class
```java
@EnableRabbit
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

##### 2. pom.xml
Add the reference to the repository, and the necessary libs:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
        <groupId>com.free-now.spring.multirabbit</groupId>
        <artifactId>spring-multirabbit-lib</artifactId>
        <version>${multirabbit.version}</version>
    </dependency>
</dependencies>
```

##### 3. application.yml
```yaml
spring:
    rabbitmq:
        host: 10.0.0.10
        port: 5672
    multirabbitmq:
        connections:
            connectionNameA:
                host: 200.10.10.10
                port: 5672
            connectionNameB:
                host: 173.49.20.18
                port: 5672
```

##### 4.1. Change context when using RabbitTemplate
```java
@Autowired
private RabbitTemplate rabbitTemplate;

void someMethod() {
    SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "connectionNameA");
    try {
        rabbitTemplate.convertAndSend("someExchange", "someRoutingKey", "someMessage"); // Use RabbitTemplate
    } finally {
        SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
    }
}
```

##### 4.2. Change context on RabbitListener
```java
/**
 * Listener for the default connection. 
 */
@RabbitListener(queues = "someQueue")
void someListener(String message) {
    // Consume message
}

/**
 * Listener for the broker tagged as `connectionNameA`. 
 */
@RabbitListener(queues = "anotherQueue", containerFactory = "connectionNameA") 
void anotherListener(String message) {
    // Consume message
}
```

## Configuration Interactions
This library enables the possibility of having multiple RabbitMQ brokers, configured from the property
**spring.multirabbitmq**. However, for maximum compatibility, it does not change the default capacity of configuring a
connection with the existent **spring.rabbitmq** property.

Thus, it's important to understand how the application will behave when multiple configurations are provided:
* Unlimited number of connections can be set, but the user must be aware of the implications of maintaining them;
* The default server **spring.rabbitmq** will always exist, even if not explicitly defined.
  * **EXCEPT** when one server under **spring.multirabbit** is set with **defaultConnection: true**. In this case, 
  any configuration provided with the default **spring.rabbitmq** will be ignored.
* It's not possible to have more than one connection under **spring.multirabbit** being set with **defaultConnection: 
true**.




#### Only spring.rabbitmq
This is the most simple scenario where only one Rabbit server is needed. In this case, the application does not even 
need the **spring-multirabbit-lib** library. No different behavior is expected and no need for Rabbit context change. 
```yaml
spring:
    rabbitmq:
        host: localhost
        port: 5672
``` 

#### Only spring.multirabbitmq (no default defined)
This scenario is tricky and must be well understood, since the behavior of Spring will enforce the creation of the 
default connection using **spring.rabbitmq** even when it's not provided. The same will happen here. The following 
example will instantiate 3 connections, which one of them is the default, configured with default parameters. To use 
any of the connections under **spring.multirabbitmq.connections**, the connection factory context must be provided.
```yaml
spring:
    multirabbitmq:
        connections:
            connectionNameA:
                host: localhost
                port: 5673
            connectionNameB:
                host: localhost
                port: 5674
```

#### Only spring.multirabbitmq (one default present)
This scenario is pretty straightforward, since there will be no implicit connection. In this case, there will exist 
2 connections, and the connection **connectionNameA** can be accessed with or without definition of context, since it's
the default connection.
```yaml
spring:
    multirabbitmq:
        defaultConnection: connectionNameA
        connections:
            connectionNameA:
                host: localhost
                port: 5673
            connectionNameB:
                host: localhost
                port: 5674
```

#### Both spring.rabbitmq and spring.multirabbitmq (no explicit default)
In this scenario, 3 connections will be available, the ones under **spring.multirabbit** will require context. 
```yaml
spring:
    rabbitmq:
        host: localhost
        port: 5672
    multirabbitmq:
        connections:
            connectionNameA:
                host: localhost
                port: 5673
            connectionNameB:
                host: localhost
                port: 5674
```

#### Both spring.rabbitmq and spring.multirabbitmq (with one explicit default)
In this scenario, only 2 connections will be available. Since there is one set explicitly as default, the one 
provided at **spring.rabbitmq** will be ignored. The connection **connectionNameA** can be accessed with or without 
Rabbit context.
```yaml
spring:
    rabbitmq:
        host: localhost
        port: 5672
    multirabbitmq:
        connections:
            connectionNameA:
                defaultConnection: true
                host: localhost
                port: 5672
            connectionNameB:
                host: localhost
                port: 5672
```

## Compatibility of versions
For the table of compatibility, please visit the [Wiki](https://github.com/freenowtech/spring-multirabbit/wiki) page.

## More Examples
More examples can be found at the modules **spring-multirabbit-example-java** and **spring-multirabbit-example-kotlin**.
As listeners are defined, they will try to connect in 3 different addresses:
* **localhost:5672**;
* **localhost:5673**; 
* **localhost:5674**.

To easily provide RabbitMQ connections for the applications, you can create 3 Docker containers:
```bash
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management; \
docker run -d -p 5673:5672 -p 15673:15672 rabbitmq:3-management; \
docker run -d -p 5674:5672 -p 15674:15672 rabbitmq:3-management
```

You need to create the Exchange, Queue and Bind them with a Routing Key:
* Exchange: **sampleExchange**
* Queue: **sampleQueue**
* Routing Key: **sampleRoutingKey**

To send messages to the different servers, run:
```bash
curl -X POST http://localhost:8080 -H 'Content-Type: application/json' -d "someMessage"
curl -X POST http://localhost:8080?connection=connectionNameA -H 'Content-Type: application/json' -d "someMessage"
curl -X POST http://localhost:8080?connection=connectionNameB -H 'Content-Type: application/json' -d "someMessage"
```
