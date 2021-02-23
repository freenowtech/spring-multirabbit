package com.freenow.multirabbit.example

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

@EnableRabbit
@SpringBootApplication
class Application
