package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.config.builder.CacheConfig
import io.github.robertomike.super_controller.config.builder.SuperControllerBuilder
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.policies.BasePolicy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.RequestMethod
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SuperControllerBuilderTest {

    private lateinit var mockService: BasicService<TestEntity, Page<TestEntity>, Long, Request, Request, Unit>
    private lateinit var mockPolicy: BasePolicy<TestEntity, Request, Request, *>
    private lateinit var mockMapper: Any

    @BeforeEach
    fun setup() {
        @Suppress("UNCHECKED_CAST")
        mockService = mock(BasicService::class.java) as BasicService<TestEntity, Page<TestEntity>, Long, Request, Request, Unit>
        @Suppress("UNCHECKED_CAST")
        mockPolicy = mock(BasePolicy::class.java) as BasePolicy<TestEntity, Request, Request, *>
        mockMapper = mock(Any::class.java)
    }

    @Test
    fun `should create builder with create factory method`() {
        val builder = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()

        assertNotNull(builder)
    }

    @Test
    fun `should build minimal configuration`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withBasePackage("io.github.robertomike")
            .build()

        assertNotNull(config)
        assertEquals("io.github.robertomike", config.basePackage)
        assertTrue(config.needAuthorization)
    }

    @Test
    fun `should throw when base package is missing`() {
        val builder = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()

        val exception = assertThrows<IllegalArgumentException> {
            builder.build()
        }

        assertEquals("Base package is required. Use withBasePackage()", exception.message)
    }

    @Test
    fun `withService should set service`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withService(mockService)
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals(mockService, config.service)
    }

    @Test
    fun `withAuthorization should enable authorization`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withAuthorization(true)
            .withBasePackage("io.github.robertomike")
            .build()

        assertTrue(config.needAuthorization)
    }

    @Test
    fun `withAuthorization should disable authorization`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withAuthorization(false)
            .withBasePackage("io.github.robertomike")
            .build()

        assertFalse(config.needAuthorization)
    }

    @Test
    fun `withPolicy should set policy`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withPolicy(mockPolicy)
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals(mockPolicy, config.policy)
    }

    @Test
    fun `withBasePackage should set base package`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withBasePackage("com.example")
            .build()

        assertEquals("com.example", config.basePackage)
    }

    @Test
    fun `withMapper should set mapper`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withMapper(mockMapper)
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals(mockMapper, config.mapper)
    }

    @Test
    fun `withCache should set cache config`() {
        val cacheConfig = CacheConfig(cacheName = "testCache", ttl = 3600)
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withCache(cacheConfig)
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals(cacheConfig, config.cacheConfig)
    }

    @Test
    fun `only should set only methods`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .only(Methods.INDEX, Methods.SHOW, Methods.STORE)
            .withBasePackage("io.github.robertomike")
            .build()

        assertNotNull(config.onlyUrls)
        assertEquals(3, config.onlyUrls!!.size)
        assertTrue(config.onlyUrls!!.contains(Methods.INDEX))
        assertTrue(config.onlyUrls!!.contains(Methods.SHOW))
        assertTrue(config.onlyUrls!!.contains(Methods.STORE))
    }

    @Test
    fun `except should set except methods`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .except(Methods.DESTROY)
            .withBasePackage("io.github.robertomike")
            .build()

        assertNotNull(config.exceptUrls)
        assertEquals(1, config.exceptUrls!!.size)
        assertTrue(config.exceptUrls!!.contains(Methods.DESTROY))
    }

    @Test
    fun `should throw when using both only and except`() {
        val builder = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .only(Methods.INDEX)

        val exception = assertThrows<IllegalArgumentException> {
            builder.except(Methods.DESTROY)
        }

        assertEquals("Cannot use both only() and except()", exception.message)
    }

    @Test
    fun `should throw when using except after only`() {
        val builder = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .except(Methods.DESTROY)

        val exception = assertThrows<IllegalArgumentException> {
            builder.only(Methods.INDEX)
        }

        assertEquals("Cannot use both only() and except()", exception.message)
    }

    @Test
    fun `addCustomRoute should add custom route`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .addCustomRoute("/custom", RequestMethod.POST, "customHandler", "Custom route")
            .withBasePackage("io.github.robertomike")
            .build()

        assertNotNull(config.customRoutes)
        assertEquals(1, config.customRoutes.size)
        assertTrue(config.customRoutes.containsKey("/custom"))
        
        val route = config.customRoutes["/custom"]!!
        assertEquals("/custom", route.path)
        assertEquals(RequestMethod.POST, route.method)
        assertEquals("customHandler", route.handlerMethod)
        assertEquals("Custom route", route.description)
    }

    @Test
    fun `addCustomRoute should support multiple custom routes`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .addCustomRoute("/activate", RequestMethod.POST, "activate")
            .addCustomRoute("/deactivate", RequestMethod.POST, "deactivate")
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals(2, config.customRoutes.size)
        assertTrue(config.customRoutes.containsKey("/activate"))
        assertTrue(config.customRoutes.containsKey("/deactivate"))
    }

    @Test
    fun `enableSoftDelete should enable soft delete`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .enableSoftDelete()
            .withBasePackage("io.github.robertomike")
            .build()

        assertTrue(config.enableSoftDelete)
    }

    @Test
    fun `disableSoftDelete should disable soft delete`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .enableSoftDelete()
            .disableSoftDelete()
            .withBasePackage("io.github.robertomike")
            .build()

        assertFalse(config.enableSoftDelete)
    }

    @Test
    fun `enableBulkOperations should enable bulk operations`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .enableBulkOperations()
            .withBasePackage("io.github.robertomike")
            .build()

        assertTrue(config.enableBulkOperations)
    }

    @Test
    fun `disableBulkOperations should disable bulk operations`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .enableBulkOperations()
            .disableBulkOperations()
            .withBasePackage("io.github.robertomike")
            .build()

        assertFalse(config.enableBulkOperations)
    }

    @Test
    fun `withApiVersion should set API version`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withApiVersion("v2")
            .withBasePackage("io.github.robertomike")
            .build()

        assertEquals("v2", config.apiVersion)
    }

    @Test
    fun `should support method chaining`() {
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withService(mockService)
            .withAuthorization(true)
            .withPolicy(mockPolicy)
            .withBasePackage("io.github.robertomike")
            .withMapper(mockMapper)
            .withApiVersion("v1")
            .only(Methods.INDEX, Methods.SHOW)
            .enableSoftDelete()
            .enableBulkOperations()
            .addCustomRoute("/activate", RequestMethod.POST, "activate")
            .build()

        assertEquals(mockService, config.service)
        assertTrue(config.needAuthorization)
        assertEquals(mockPolicy, config.policy)
        assertEquals("io.github.robertomike", config.basePackage)
        assertEquals(mockMapper, config.mapper)
        assertEquals("v1", config.apiVersion)
        assertEquals(2, config.onlyUrls!!.size)
        assertTrue(config.enableSoftDelete)
        assertTrue(config.enableBulkOperations)
        assertEquals(1, config.customRoutes.size)
    }

    @Test
    fun `should build complete configuration`() {
        val cacheConfig = CacheConfig(cacheName = "entities", ttl = 1800)
        
        val config = SuperControllerBuilder.create<TestEntity, Long, Request, Request>()
            .withService(mockService)
            .withAuthorization(false)
            .withPolicy(mockPolicy)
            .withBasePackage("io.github.robertomike.super_controller")
            .withMapper(mockMapper)
            .withCache(cacheConfig)
            .only(Methods.INDEX, Methods.SHOW, Methods.STORE, Methods.UPDATE)
            .addCustomRoute("/custom1", RequestMethod.POST, "handler1", "Description 1")
            .addCustomRoute("/custom2", RequestMethod.GET, "handler2", "Description 2")
            .enableSoftDelete()
            .enableBulkOperations()
            .withApiVersion("v1")
            .build()

        assertNotNull(config)
        assertEquals(mockService, config.service)
        assertFalse(config.needAuthorization)
        assertEquals(mockPolicy, config.policy)
        assertEquals("io.github.robertomike.super_controller", config.basePackage)
        assertEquals(mockMapper, config.mapper)
        assertEquals(cacheConfig, config.cacheConfig)
        assertEquals(4, config.onlyUrls!!.size)
        assertEquals(2, config.customRoutes.size)
        assertTrue(config.enableSoftDelete)
        assertTrue(config.enableBulkOperations)
        assertEquals("v1", config.apiVersion)
    }

    // Test entity
    private data class TestEntity(val id: Long, val name: String)
}
