package io.github.robertomike.super_controller.config

import io.github.robertomike.super_controller.Application
import org.modelmapper.ModelMapper
import org.modelmapper.convention.MatchingStrategies
import org.springframework.context.annotation.Bean
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
    /**
     * Creates a [ModelMapper] bean with a strict matching strategy.
     *
     * The [ModelMapper] is used to map between different JavaBean objects.
     * The strict matching strategy ensures that only properties with the same name and type are mapped.
     *
     * @return a [ModelMapper] instance with a strict matching strategy
     * @see ModelMapper
     * @see MatchingStrategies.STRICT
     */
    @Bean
    open fun modelMapper(): ModelMapper {
        val modelMapper = ModelMapper()
        modelMapper.configuration.setMatchingStrategy(MatchingStrategies.STRICT)
        return modelMapper
    }
}