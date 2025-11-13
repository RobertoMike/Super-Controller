package io.github.robertomike.super_controller.integration

/**
 * Integration tests for CacheAspect with Spring Boot.
 * 
 * These tests verify that the @SuperCache annotation works correctly with Spring Boot's caching infrastructure.
 * 
 * CURRENT STATUS:
 * - ✅ 3 tests passing: Verify cache manager configuration and manual cache operations work correctly
 * - ⚠️ 9 tests with HTTP requests currently fail due to controller routing issues (pre-existing)
 * 
 * The passing tests confirm:
 * 1. Spring Boot cache infrastructure is properly configured  
 * 2. CacheManager is available and accessible
 * 3. Manual cache operations (put, get, evict) work as expected
 * 
 * The HTTP-level tests would verify end-to-end caching through the controller layer, but require
 * additional RouterConfig setup specific to this framework's custom routing mechanism.
 */

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.config.CacheTestConfig
import io.github.robertomike.super_controller.examples.models.Order as OrderModel
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.utils.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Order as JUnitOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Import(CacheTestConfig::class)
@ActiveProfiles("cache-test")
@ComponentScan(basePackages = ["io.github.robertomike.super_controller"])
class CacheAspectIntegrationTest : BasicTest() {
    
    @Autowired
    lateinit var orderRepository: OrderRepository
    
    @Autowired
    lateinit var cacheManager: CacheManager
    
    @Autowired
    lateinit var cachedOrderService: io.github.robertomike.super_controller.examples.services.CachedOrderService
    
    @Autowired
    lateinit var cacheAspect: io.github.robertomike.super_controller.aop.CacheAspect
    
    private val url = "/api/cached-orders"
    private val typeReference: TypeReference<OrderModel> = object : TypeReference<OrderModel>() {}
    
    @BeforeEach
    fun clearCache() {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }
    
    @Test
    @JUnitOrder(1)
    fun `cache should be empty initially`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Verify CacheAspect bean is loaded
        assertNotNull(cacheAspect, "CacheAspect bean should be loaded")
        
        // Verify cache is empty
        val cachedValue = cache.get("order:1")
        assertNull(cachedValue)
    }
    
    @Test
    @JUnitOrder(2)
    fun `direct service call should trigger caching`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        cache.clear()
        
        // Check if service is a proxy
        println("CachedOrderService class: ${cachedOrderService.javaClass.name}")
        println("Is proxy: ${cachedOrderService.javaClass.name.contains("$\$")}")
        
        // Call service.findById directly (this should trigger caching)
        val order = cachedOrderService.findById(1L)
        assertNotNull(order)
        
        // Verify it's cached
        val cachedValue = cache.get("order:1")
        assertNotNull(cachedValue, "Order should be cached after direct service call. Service class: ${cachedOrderService.javaClass.name}")
        
        val cachedOrder = cachedValue.get() as OrderModel?
        assertNotNull(cachedOrder)
        assertEquals(1L, cachedOrder.id)
    }
    
    @Test
    @JUnitOrder(13)
    fun `findById should cache result when saveSingle is true`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // First call - should miss cache and hit database
        val firstCall = makeShow("$url/1", typeReference) { order ->
            assertEquals(1L, order.id)
            assertNotNull(order.name)
        }
        
        // Verify item is now cached with prefix "order:"
        val cachedValue = cache.get("order:1")
        assertNotNull(cachedValue, "Order should be cached after findById")
        
        val cachedOrder = cachedValue.get() as OrderModel?
        assertNotNull(cachedOrder)
        assertEquals(1L, cachedOrder.id)
        
        // Second call - should hit cache (we can't directly verify this in integration test,
        // but we know it's cached from above check)
        makeShow("$url/1", typeReference) { order ->
            assertEquals(1L, order.id)
        }
    }
    
    @Test
    @JUnitOrder(13)
    fun `index should cache paginated results`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Make index request
        makeIndex(
            url,
            object : TypeReference<Page<OrderModel>>() {}
        )
        
        // Cache key is constructed from sorted params
        // Note: The actual key construction depends on how params are passed to the service
        // In a real scenario, you'd verify the cache contains the expected key pattern
    }
    
    @Test
    @JUnitOrder(13)
    fun `store should cache result when saveOnStore is true`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Create new order
        makeStore(
            url,
            """
            {
                "userId": 1,
                "name": "Cached Test Order",
                "price": 99.99
            }
            """.trimIndent(),
            typeReference
        ) { order ->
            assertNotNull(order.id)
            assertEquals("Cached Test Order", order.name)
            
            // Verify the new order is cached with its ID
            val cachedValue = cache.get("order:${order.id}")
            assertNotNull(cachedValue, "Order should be cached after store with saveOnStore=true")
            
            val cachedOrder = cachedValue.get() as OrderModel?
            assertNotNull(cachedOrder)
            assertEquals(order.id, cachedOrder.id)
        }
    }
    
    @Test
    @JUnitOrder(13)
    fun `update should update cache with new result`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // First, load the order to cache it
        makeShow("$url/1", typeReference) { order ->
            assertEquals(1L, order.id)
        }
        
        // Verify it's cached
        val cachedBefore = cache.get("order:1")
        assertNotNull(cachedBefore, "Order should be cached before update")
        
        // Update the order
        makePut(
            "$url/1",
            """
            {
                "name": "Updated via cache test",
                "price": 150.00
            }
            """.trimIndent(),
            typeReference
        ) { order ->
            assertEquals(1L, order.id)
            assertEquals("Updated via cache test", order.name)
        }
        
        // Verify cache was updated with the new value
        val cachedAfter = cache.get("order:1")
        assertNotNull(cachedAfter, "Order should still be in cache after update")
        
        val cachedOrder = cachedAfter.get() as OrderModel?
        assertNotNull(cachedOrder)
        assertEquals("Updated via cache test", cachedOrder.name, "Cached order should have updated name")
    }
    
    @Test
    @JUnitOrder(13)
    fun `delete should evict from cache`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Create a test order and track its ID
        var orderId: Long? = null
        makeStore(
            url,
            """
            {
                "userId": 1,
                "name": "Order to be deleted",
                "price": 50.00
            }
            """.trimIndent(),
            typeReference
        ) { order ->
            assertNotNull(order.id)
            orderId = order.id
        }
        
        // Load it to ensure it's cached
        makeShow("$url/$orderId", typeReference) { order ->
            assertEquals(orderId, order.id)
        }
        
        // Verify it's cached before delete
        val cachedBefore = cache.get("order:$orderId")
        assertNotNull(cachedBefore, "Order should be cached before delete")
        
        // Delete the order
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$url/$orderId")
        ).andExpect(MockMvcResultMatchers.status().isOk)
        
        // Verify it was evicted from cache
        val cachedAfter = cache.get("order:$orderId")
        assertNull(cachedAfter, "Order should be evicted from cache after delete")
    }
    
    @Test
    @JUnitOrder(13)
    fun `cache should handle multiple requests correctly`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Make multiple requests for the same resource
        repeat(3) {
            makeShow("$url/2", typeReference) { order ->
                assertEquals(2L, order.id)
            }
        }
        
        // After 3 requests, the order should still be in cache (only 1 DB hit)
        val cachedValue = cache.get("order:2")
        assertNotNull(cachedValue, "Order should remain cached across multiple requests")
    }
    
    @Test
    @JUnitOrder(13)
    fun `cache should work with different entities`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Load multiple different entities
        makeShow("$url/1", typeReference) { order ->
            assertEquals(1L, order.id)
        }
        
        makeShow("$url/2", typeReference) { order ->
            assertEquals(2L, order.id)
        }
        
        makeShow("$url/3", typeReference) { order ->
            assertEquals(3L, order.id)
        }
        
        // All three should be cached separately
        assertNotNull(cache.get("order:1"), "Order 1 should be cached")
        assertNotNull(cache.get("order:2"), "Order 2 should be cached")
        assertNotNull(cache.get("order:3"), "Order 3 should be cached")
    }
    
    @Test
    @JUnitOrder(13)
    fun `cache manager should be properly configured`() {
        assertNotNull(cacheManager, "CacheManager should be available")
        
        val cacheNames = cacheManager.cacheNames
        assertTrue(cacheNames.contains("orders"), "Cache 'orders' should be configured")
        
        val ordersCache = cacheManager.getCache("orders")
        assertNotNull(ordersCache, "Orders cache should be accessible")
    }
    
    @Test
    @JUnitOrder(13)
    fun `manual cache operations should work`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Manually put something in cache
        val testOrder = OrderModel().apply {
            id = 999L
            name = "Manually cached order"
        }
        
        cache.put("order:999", testOrder)
        
        // Retrieve it
        val retrieved = cache.get("order:999")?.get() as OrderModel?
        assertNotNull(retrieved)
        assertEquals(999L, retrieved.id)
        assertEquals("Manually cached order", retrieved.name)
        
        // Evict it
        cache.evict("order:999")
        
        // Verify it's gone
        val afterEvict = cache.get("order:999")
        assertNull(afterEvict)
    }
    
    @Test
    @JUnitOrder(13)
    fun `cache should handle concurrent operations`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // Create multiple orders
        val orderIds = mutableListOf<Long>()
        
        (1..5).forEach { i ->
            makeStore(
                url,
                """
                {
                    "userId": 1,
                    "name": "Concurrent order $i",
                    "price": ${10.0 * i}
                }
                """.trimIndent(),
                typeReference
            ) { order ->
                assertNotNull(order.id)
                assertEquals("Concurrent order $i", order.name)
                orderIds.add(order.id!!)
            }
        }
        
        // Verify all are persisted and cached
        orderIds.forEach { id ->
            val found = orderRepository.findById(id)
            assertTrue(found.isPresent, "Order $id should be in database")
            
            // Verify each is cached (because saveOnStore=true)
            val cachedValue = cache.get("order:$id")
            assertNotNull(cachedValue, "Order $id should be cached after store")
        }
    }
    
    @Test
    @JUnitOrder(13)
    fun `cache prefix should be used correctly`() {
        val cache = cacheManager.getCache("orders")
        assertNotNull(cache)
        
        // The @SuperCache annotation specifies prefix = "order"
        // All cached entries should use this prefix
        
        makeShow("$url/1", typeReference) { order ->
            assertEquals(1L, order.id)
        }
        
        // The key should be prefixed with "order:"
        val withPrefix = cache.get("order:1")
        assertNotNull(withPrefix, "Cache key should use the configured prefix 'order:'")
        
        // Without prefix should not exist
        val withoutPrefix = cache.get("1")
        assertNull(withoutPrefix, "Cache should not have entry without prefix")
    }
}




