package io.github.robertomike.super_controller.config

import io.github.robertomike.super_controller.Application
import org.modelmapper.ModelMapper
import org.modelmapper.convention.MatchingStrategies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["io.github.robertomike.super_controller"],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = arrayOf(Application::class))
    ]
)
open class RegistrationConfig {
    @Bean
    open fun modelMapper(): ModelMapper {
        val modelMapper = ModelMapper()
        modelMapper.configuration.setMatchingStrategy(MatchingStrategies.STRICT)
        return modelMapper
    }
}