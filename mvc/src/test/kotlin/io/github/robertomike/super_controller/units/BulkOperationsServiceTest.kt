package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.bulk.BulkOperations
import io.github.robertomike.super_controller.services.bulk.BulkDeleteResult
import io.github.robertomike.super_controller.services.bulk.BulkResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BulkOperationsServiceTest {

    private lateinit var service: TestBulkService

    @BeforeEach
    fun setup() {
        service = TestBulkService()
    }

    @Test
    fun `bulkStore should successfully store all items`() {
        val requests = listOf(
            TestRequest("user1"),
            TestRequest("user2"),
            TestRequest("user3")
        )

        val result = service.bulkStore(requests)

        assertEquals(3, result.successCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.allSuccessful)
        assertEquals(3, result.successful.size)
    }

    @Test
    fun `bulkStore should handle partial failures`() {
        service.failOnIndex = 1

        val requests = listOf(
            TestRequest("user1"),
            TestRequest("fail"),
            TestRequest("user3")
        )

        val result = service.bulkStore(requests)

        assertEquals(2, result.successCount)
        assertEquals(1, result.failedCount)
        assertTrue(result.anyFailed)
        assertEquals(1, result.failed[0].index)
    }

    @Test
    fun `bulkStore should call lifecycle hooks`() {
        val requests = listOf(TestRequest("user1"))

        service.bulkStore(requests)

        assertTrue(service.beforeBulkStoreCalled)
        assertTrue(service.afterBulkStoreCalled)
    }

    @Test
    fun `bulkStore should calculate correct success rate`() {
        service.failOnIndex = 1

        val requests = listOf(
            TestRequest("user1"),
            TestRequest("fail"),
            TestRequest("user3"),
            TestRequest("user4")
        )

        val result = service.bulkStore(requests)

        assertEquals(75.0, result.successRate)
    }

    @Test
    fun `bulkUpdate should successfully update all items`() {
        val updates = mapOf(
            1L to TestRequest("updated1"),
            2L to TestRequest("updated2")
        )

        val result = service.bulkUpdate(updates)

        assertEquals(2, result.successCount)
        assertEquals(0, result.failedCount)
    }

    @Test
    fun `bulkUpdate should handle not found items`() {
        service.failOnIndex = 0

        val updates = mapOf(
            999L to TestRequest("not found"),
            2L to TestRequest("updated2")
        )

        val result = service.bulkUpdate(updates)

        assertEquals(1, result.successCount)
        assertEquals(1, result.failedCount)
    }

    @Test
    fun `bulkUpdate should call lifecycle hooks`() {
        val updates = mapOf(1L to TestRequest("updated"))

        service.bulkUpdate(updates)

        assertTrue(service.beforeBulkUpdateCalled)
        assertTrue(service.afterBulkUpdateCalled)
    }

    @Test
    fun `bulkDelete should successfully delete all items`() {
        val ids = listOf(1L, 2L, 3L)

        val result = service.bulkDelete(ids)

        assertEquals(3, result.deletedCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.allSuccessful)
    }

    @Test
    fun `bulkDelete should handle non-existent IDs`() {
        service.failOnIndex = 1

        val ids = listOf(1L, 999L, 3L)

        val result = service.bulkDelete(ids)

        assertEquals(2, result.deletedCount)
        assertEquals(1, result.failedCount)
        assertEquals(1, result.failedIds.size)
    }

    @Test
    fun `bulkDelete should call lifecycle hooks`() {
        val ids = listOf(1L)

        service.bulkDelete(ids)

        assertTrue(service.beforeBulkDeleteCalled)
        assertTrue(service.afterBulkDeleteCalled)
    }

    @Test
    fun `bulkDelete with all failures should return correct counts`() {
        service.alwaysFail = true

        val ids = listOf(1L, 2L, 3L)

        val result = service.bulkDelete(ids)

        assertEquals(0, result.deletedCount)
        assertEquals(3, result.failedCount)
        assertEquals(3, result.failedIds.size)
    }

    @Test
    fun `bulkResult should track total processed correctly`() {
        service.failOnIndex = 2

        val requests = listOf(
            TestRequest("user1"),
            TestRequest("user2"),
            TestRequest("fail"),
            TestRequest("user4")
        )

        val result = service.bulkStore(requests)

        assertEquals(4, result.totalProcessed)
        assertEquals(3, result.successCount)
        assertEquals(1, result.failedCount)
    }

    // Test implementation
    private data class TestRequest(val name: String) : Request

    private class TestBulkService : BulkOperations<User, Page<User>, Long, TestRequest, TestRequest> {
        var failOnIndex = -1
        var alwaysFail = false
        var beforeBulkStoreCalled = false
        var afterBulkStoreCalled = false
        var beforeBulkUpdateCalled = false
        var afterBulkUpdateCalled = false
        var beforeBulkDeleteCalled = false
        var afterBulkDeleteCalled = false
        private var currentIndex = 0

        override fun store(request: TestRequest): User {
            val idx = currentIndex++
            if (alwaysFail || idx == failOnIndex) {
                throw RuntimeException("Store failed at index $idx")
            }
            return User().apply {
                id = idx.toLong()
                name = request.name
            }
        }

        override fun findById(id: Long): User {
            val idx = currentIndex++
            if (alwaysFail || idx == failOnIndex) {
                throw NotFoundException("User not found: $id")
            }
            return User().apply {
                this.id = id
                name = "User $id"
            }
        }

        override fun update(model: User, request: TestRequest): User {
            model.name = request.name
            return model
        }

        override fun delete(model: User) {
            // Simulate deletion
        }

        override fun beforeBulkStore(request: TestRequest) {
            beforeBulkStoreCalled = true
        }

        override fun afterBulkStore(entity: User, request: TestRequest) {
            afterBulkStoreCalled = true
        }

        override fun beforeBulkUpdate(entity: User, request: TestRequest) {
            beforeBulkUpdateCalled = true
        }

        override fun afterBulkUpdate(entity: User, request: TestRequest) {
            afterBulkUpdateCalled = true
        }

        override fun beforeBulkDelete(entity: User) {
            beforeBulkDeleteCalled = true
        }

        override fun afterBulkDelete(id: Long) {
            afterBulkDeleteCalled = true
        }

        override fun index(page: PageRequest, params: Map<String, String>): Page<User> {
            throw NotImplementedError()
        }

        override fun show(model: User) = model
    }
}
