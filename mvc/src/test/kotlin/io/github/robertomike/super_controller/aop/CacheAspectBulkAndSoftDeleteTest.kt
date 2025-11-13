package io.github.robertomike.super_controller.aop

import io.github.robertomike.super_controller.SuperCache
import io.github.robertomike.super_controller.services.bulk.BulkDeleteResult
import io.github.robertomike.super_controller.services.bulk.BulkError
import io.github.robertomike.super_controller.services.bulk.BulkResult
import jakarta.persistence.Id
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import kotlin.test.assertEquals

/**
 * Unit tests for CacheAspect support for bulk operations and soft delete.
 */
class CacheAspectBulkAndSoftDeleteTest {
    
    private lateinit var cacheManager: CacheManager
    private lateinit var aspect: CacheAspect
    
    // Test entity with @Id annotation
    class TestEntity {
        @Id
        var id: Long = 0
        var name: String = ""
    }
    
    // Test service with @SuperCache annotation
    @SuperCache(value = "test-cache", prefix = "test", saveIndex = true, saveSingle = true, saveOnStore = true)
    class TestService
    
    @BeforeEach
    fun setup() {
        cacheManager = mock(CacheManager::class.java)
        aspect = CacheAspect(cacheManager)
    }
    
    // ==================== Bulk Store Tests ====================
    
    @Test
    fun `aroundBulkStore should cache all successful entities when saveOnStore is true`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val entity1 = TestEntity().apply { id = 1; name = "Entity 1" }
        val entity2 = TestEntity().apply { id = 2; name = "Entity 2" }
        val bulkResult = BulkResult(
            successful = listOf(entity1, entity2),
            failed = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundBulkStore(pjp, emptyList<Any>())
        
        assertEquals(bulkResult, result)
        verify(cache).put("test:1", entity1)
        verify(cache).put("test:2", entity2)
    }
    
    @Test
    fun `aroundBulkStore should not cache when saveOnStore is false`() {
        // Create a service with saveOnStore=false
        @SuperCache(value = "test-cache", prefix = "test", saveIndex = false, saveSingle = false, saveOnStore = false)
        class NoStoreService
        
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = NoStoreService()
        val cache = mock(Cache::class.java)
        
        val entity = TestEntity().apply { id = 1; name = "Entity 1" }
        val bulkResult = BulkResult(
            successful = listOf(entity),
            failed = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        aspect.aroundBulkStore(pjp, emptyList<Any>())
        
        verify(cache, never()).put(anyString(), any())
    }
    
    @Test
    fun `aroundBulkStore should handle empty successful list`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val bulkResult = BulkResult<TestEntity>(
            successful = emptyList(),
            failed = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        aspect.aroundBulkStore(pjp, emptyList<Any>())
        
        verify(cache, never()).put(anyString(), any())
    }
    
    // ==================== Bulk Update Tests ====================
    
    @Test
    fun `aroundBulkUpdate should update cache for all successful entities`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val entity1 = TestEntity().apply { id = 1; name = "Updated 1" }
        val entity2 = TestEntity().apply { id = 2; name = "Updated 2" }
        val bulkResult = BulkResult(
            successful = listOf(entity1, entity2),
            failed = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundBulkUpdate(pjp, emptyMap<Any, Any>())
        
        assertEquals(bulkResult, result)
        verify(cache).put("test:1", entity1)
        verify(cache).put("test:2", entity2)
    }
    
    @Test
    fun `aroundBulkUpdate should handle null entities in result`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val entity = TestEntity().apply { id = 1; name = "Updated" }
        val bulkResult = BulkResult(
            successful = listOf(entity, null as TestEntity?),
            failed = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        aspect.aroundBulkUpdate(pjp, emptyMap<Any, Any>())
        
        // Should only cache the non-null entity
        verify(cache).put("test:1", entity)
        verify(cache, times(1)).put(anyString(), any())
    }
    
    // ==================== Bulk Delete Tests ====================
    
    @Test
    fun `aroundBulkDelete should evict all successfully deleted IDs from cache`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val bulkDeleteResult = BulkDeleteResult(
            deletedCount = 2,
            failedIds = emptyList(),
            errors = emptyList()
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkDeleteResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val idsToDelete = listOf(1L, 2L)
        val result = aspect.aroundBulkDelete(pjp, idsToDelete)
        
        assertEquals(bulkDeleteResult, result)
        verify(cache).evict("test:1")
        verify(cache).evict("test:2")
    }
    
    @Test
    fun `aroundBulkDelete should not evict failed IDs from cache`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val bulkDeleteResult = BulkDeleteResult(
            deletedCount = 1,
            failedIds = listOf(2L),
            errors = listOf(BulkError(1, "Failed to delete"))
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkDeleteResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val idsToDelete = listOf(1L, 2L)
        aspect.aroundBulkDelete(pjp, idsToDelete)
        
        // Should only evict ID 1 (successful), not ID 2 (failed)
        verify(cache).evict("test:1")
        verify(cache, never()).evict("test:2")
    }
    
    @Test
    fun `aroundBulkDelete should handle all IDs failing gracefully`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        val bulkDeleteResult = BulkDeleteResult(
            deletedCount = 0,
            failedIds = listOf(1L, 2L),
            errors = listOf(BulkError(0, "Failed"), BulkError(1, "Failed"))
        )
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(bulkDeleteResult)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val idsToDelete = listOf(1L, 2L)
        aspect.aroundBulkDelete(pjp, idsToDelete)
        
        // Should not evict any IDs since all failed
        verify(cache, never()).evict(anyString())
    }
    
    // ==================== Soft Delete Tests ====================
    
    @Test
    fun `aroundSoftDelete should evict entity from cache`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        val entity = TestEntity().apply { id = 1; name = "To Delete" }
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundSoftDelete(pjp, 1L)
        
        assertEquals(entity, result)
        verify(cache).evict("test:1")
    }
    
    // ==================== Restore Tests ====================
    
    @Test
    fun `aroundRestore should cache restored entity when saveSingle is true`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        val entity = TestEntity().apply { id = 1; name = "Restored" }
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundRestore(pjp, 1L)
        
        assertEquals(entity, result)
        verify(cache).put("test:1", entity)
    }
    
    @Test
    fun `aroundRestore should not cache when saveSingle is false`() {
        // Create a service with saveSingle=false
        @SuperCache(value = "test-cache", prefix = "test", saveIndex = false, saveSingle = false, saveOnStore = false)
        class NoSaveService
        
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = NoSaveService()
        val cache = mock(Cache::class.java)
        val entity = TestEntity().apply { id = 1; name = "Restored" }
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        aspect.aroundRestore(pjp, 1L)
        
        verify(cache, never()).put(anyString(), any())
    }
    
    // ==================== Force Delete Tests ====================
    
    @Test
    fun `aroundForceDelete should evict entity from cache`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val cache = mock(Cache::class.java)
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(Unit)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        aspect.aroundForceDelete(pjp, 1L)
        
        verify(cache).evict("test:1")
    }
}
