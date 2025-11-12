package io.github.robertomike.super_controller.extensions

import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.BulkUpdateItem
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.models.SoftDeletableEntity
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.bulk.BulkOperations
import io.github.robertomike.super_controller.services.interfaces.BasicService
import io.github.robertomike.super_controller.services.softdelete.SoftDeletableService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class ExtensionSystemTest {
//
//    data class TestModel(val id: Long = 1L, var name: String = "Test") : SoftDeletableEntity {
//        private var deletedAt: LocalDateTime? = null
//
//        override fun getDeletedAt(): LocalDateTime? = deletedAt
//        override fun markAsDeleted() { deletedAt = LocalDateTime.now() }
//        override fun restore() { deletedAt = null }
//    }
//
//    data class TestRequest(val name: String) : Request
//
//    class TestService :
//        BasicService<TestModel, Page<TestModel>, Long, Request, Request, Unit>,
//        BulkOperations<TestModel, Page<TestModel>, Long, Request, Request, Unit>,
//        SoftDeletableService<TestModel, Page<TestModel>, Long, Request, Request, Unit> {
//
//        private val storage = mutableMapOf<Long, TestModel>()
//
//        override fun index(page: PageRequest, params: Map<String, String>): Page<TestModel> {
//            return PageImpl(listOf(TestModel()))
//        }
//        override fun store(request: Request): TestModel = TestModel()
//        override fun show(model: TestModel): TestModel = model
//        override fun update(model: TestModel, request: Request): TestModel = model
//        override fun delete(model: TestModel) = Unit
//        override fun findById(id: Long): TestModel {
//            return storage.getOrPut(id) { TestModel(id) }
//        }
//    }
//
//    // Mock controller with BulkOperations
//    class TestControllerWithBulk : BulkOperationsMarker<TestModel, Long, TestRequest, TestRequest> {
//        val service = TestService()
//        override fun getBulkService() = service as BulkOperations<TestModel, *, Long, TestRequest, TestRequest, *>
//    }
//
//    // Mock controller with SoftDeletable
//    class TestControllerWithSoftDelete : SoftDeletableMarker<TestModel, Long> {
//        val service = TestService()
//        override fun getSoftDeleteService() = service as SoftDeletableService<TestModel, *, Long, *, *, *>
//    }
//
//    // Mock controller with both
//    class TestControllerWithBoth :
//        BulkOperationsMarker<TestModel, Long, TestRequest, TestRequest>,
//        SoftDeletableMarker<TestModel, Long> {
//        val service = TestService()
//        override fun getBulkService() = service as BulkOperations<TestModel, *, Long, TestRequest, TestRequest, *>
//        override fun getSoftDeleteService() = service as SoftDeletableService<TestModel, *, Long, *, *, *>
//    }
//
//    @Test
//    fun `BulkOperations interface should provide bulkStore method`() {
//        val controller = TestControllerWithBulk()
//
//        val requests = listOf(TestRequest("Test1"), TestRequest("Test2"))
//        val result = controller.bulkStore(requests)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertNotNull(result.body)
//        assertEquals(2, result.body?.totalProcessed)
//    }
//
//    @Test
//    fun `BulkOperations interface should provide bulkUpdate method`() {
//        val controller = TestControllerWithBulk()
//
//        val updates = listOf(
//            BulkUpdateItem(1L, TestRequest("Updated1")),
//            BulkUpdateItem(2L, TestRequest("Updated2"))
//        )
//        val result = controller.bulkUpdate(updates)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertNotNull(result.body)
//        assertEquals(2, result.body?.totalProcessed)
//    }
//
//    @Test
//    fun `BulkOperations interface should provide bulkDelete method`() {
//        val controller = TestControllerWithBulk()
//
//        val ids = listOf(1L, 2L, 3L)
//        val result = controller.bulkDelete(ids)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertNotNull(result.body)
//        assertEquals(3, result.body?.totalProcessed)
//    }
//
//    @Test
//    fun `SoftDeletable interface should provide softDelete method`() {
//        val controller = TestControllerWithSoftDelete()
//
//        val result = controller.softDelete(1L)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertNotNull(result.body)
//        assertTrue(result.body?.isDeleted() == true)
//    }
//
//    @Test
//    fun `SoftDeletable interface should provide restore method`() {
//        val controller = TestControllerWithSoftDelete()
//
//        // First soft delete
//        controller.softDelete(1L)
//
//        // Then restore
//        val result = controller.restore(1L)
//
//        assertEquals(HttpStatus.OK, result.statusCode)
//        assertNotNull(result.body)
//        assertFalse(result.body?.isDeleted() == true)
//    }
//
//    @Test
//    fun `SoftDeletable interface should provide forceDelete method`() {
//        val controller = TestControllerWithSoftDelete()
//
//        val result = controller.forceDelete(1L)
//
//        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
//    }
//
//    @Test
//    fun `Controller can implement both interfaces`() {
//        val controller = TestControllerWithBoth()
//
//        // Test bulk operations
//        val bulkResult = controller.bulkStore(listOf(TestRequest("Test")))
//        assertEquals(HttpStatus.OK, bulkResult.statusCode)
//
//        // Test soft delete
//        val softDeleteResult = controller.softDelete(1L)
//        assertEquals(HttpStatus.OK, softDeleteResult.statusCode)
//    }
}
