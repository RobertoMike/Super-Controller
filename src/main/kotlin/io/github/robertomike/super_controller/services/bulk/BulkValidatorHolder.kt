package io.github.robertomike.super_controller.services.bulk

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Gives [BulkOperations]'s default methods - plain interface default methods with no
 * constructor to receive a [Validator] through - access to the application's actual
 * Spring-managed [Validator] bean (the same [jakarta.validation.Validator] backing
 * `@Valid @RequestBody`), so a custom `ConstraintValidator` that relies on Spring DI or
 * on Spring's `MessageSource`-based message interpolation behaves the same way for bulk
 * items as it does for the single-item `store`/`update` endpoints.
 *
 * Falls back to a plain JSR-380 default validator when no Spring context is available
 * (e.g. a unit test that builds a service directly without a Spring context) or when the
 * context has no [Validator] bean of its own.
 */
@Component
class BulkValidatorHolder : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        @Volatile
        private var context: ApplicationContext? = null

        private val fallback by lazy { Validation.buildDefaultValidatorFactory().validator }

        val validator: Validator
            get() = try {
                context?.getBean(Validator::class.java) ?: fallback
            } catch (e: NoSuchBeanDefinitionException) {
                fallback
            }
    }
}
