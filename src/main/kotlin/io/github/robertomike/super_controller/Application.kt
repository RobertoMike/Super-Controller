package io.github.robertomike.super_controller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application


fun main(args: Array<String>) {
    runApplication<Application>(*args)
}