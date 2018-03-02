package com.idvp.elections

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.ComponentScan

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
class ApplicationConfig

object Application extends App {
    val application = new SpringApplication(classOf[ApplicationConfig])
    application.run(args: _*)
}