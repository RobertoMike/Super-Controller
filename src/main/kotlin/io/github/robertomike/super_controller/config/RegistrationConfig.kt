package io.github.robertomike.super_controller.config

import io.github.robertomike.super_controller.Application
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

/**
 * Configuration class for registering beans and components in the application context.
 *
 * This class is annotated with [Configuration] to indicate that it is a source of bean definitions.
 * It also uses [ComponentScan] to enable component scanning in the specified base packages.
 *
 */
@Configuration
@ComponentScan(
    basePackages = ["io.github.robertomike.super_controller"],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = arrayOf(Application::class))
    ]
)
open class RegistrationConfig {
}