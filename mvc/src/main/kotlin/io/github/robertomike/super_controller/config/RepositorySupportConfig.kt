package io.github.robertomike.super_controller.config

import io.github.robertomike.super_controller.repositories.RepositoryNormalSupport
import io.github.robertomike.super_controller.repositories.RepositorySupport
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RepositorySupportConfig {
    @Bean
    @ConditionalOnMissingBean(RepositorySupport::class)
    open fun repositorySupport(): RepositorySupport {
        return RepositoryNormalSupport()
    }
}