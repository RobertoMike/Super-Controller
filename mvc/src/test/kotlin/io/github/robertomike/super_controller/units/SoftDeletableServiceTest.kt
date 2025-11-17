package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.models.SoftDeletableEntity
import io.github.robertomike.super_controller.repositories.RepositorySupport
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.softdelete.SoftDeletableService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.Repository
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SoftDeletableServiceTest {

    private lateinit var service: TestSoftDeletableService
    private lateinit var repositorySupport: RepositorySupport

    @BeforeEach
    fun setup() {
        service = TestSoftDeletableService()
    }

    @Test
    fun `softDelete should mark entity as deleted`() {
        val id = 1L

        val result = service.softDelete(id)

        assertNotNull(result.deletedAt)
        assertTrue(service.beforeSoftDeleteCalled)
        assertTrue(service.afterSoftDeleteCalled)
    }

    @Test
    fun `softDelete should throw when entity already deleted`() {
        service.markAsAlreadyDeleted = true

        assertThrows<IllegalStateException> {
            service.softDelete(999L)
        }
    }

    @Test
    fun `restore should clear deletedAt timestamp`() {
        service.markAsDeleted = true
        val id = 1L

        val result = service.restore(id)

        assertNull(result.deletedAt)
        assertTrue(service.beforeRestoreCalled)
        assertTrue(service.afterRestoreCalled)
    }

    @Test
    fun `restore should throw when entity not deleted`() {
        assertThrows<IllegalStateException> {
            service.restore(1L)
        }
    }

    @Test
    fun `forceDelete should permanently delete entity`() {
        val id = 1L

        service.forceDelete(id)

        assertTrue(service.beforeForceDeleteCalled)
        assertTrue(service.afterForceDeleteCalled)
        assertTrue(service.deleteCalled)
    }

    @Test
    fun `softDelete should find entity by ID`() {
        service.softDelete(1L)

        assertTrue(service.findByIdCalled)
    }

    @Test
    fun `softDelete should throw NotFoundException for invalid ID`() {
        service.shouldThrowNotFound = true

        assertThrows<NotFoundException> {
            service.softDelete(999L)
        }
    }

    @Test
    fun `restore should find entity by ID`() {
        service.markAsDeleted = true
        service.restore(1L)

        assertTrue(service.findByIdCalled)
    }

    @Test
    fun `forceDelete should find entity by ID`() {
        service.forceDelete(1L)

        assertTrue(service.findByIdCalled)
    }

    @Test
    fun `lifecycle hooks should be called in correct order for softDelete`() {
        service.operationOrder.clear()

        service.softDelete(1L)

        assertEquals(listOf("findById", "beforeSoftDelete", "persist", "afterSoftDelete"), service.operationOrder)
    }

    @Test
    fun `lifecycle hooks should be called in correct order for restore`() {
        service.markAsDeleted = true
        service.operationOrder.clear()

        service.restore(1L)

        assertEquals(listOf("findById", "beforeRestore", "update", "afterRestore"), service.operationOrder)
    }

    @Test
    fun `lifecycle hooks should be called in correct order for forceDelete`() {
        service.operationOrder.clear()

        service.forceDelete(1L)

        assertEquals(listOf("findById", "beforeForceDelete", "delete", "afterForceDelete"), service.operationOrder)
    }

    // Test entities and service
    private data class TestEntity(val id: Long, var name: String) : SoftDeletableEntity {
        override var deletedAt: LocalDateTime? = null
    }

    private data class TestRequest(val name: String) : Request

    private class TestSoftDeletableService : SoftDeletableService<TestEntity, Page<TestEntity>, Long, TestRequest, TestRequest> {
        var beforeSoftDeleteCalled = false
        var afterSoftDeleteCalled = false
        var beforeRestoreCalled = false
        var afterRestoreCalled = false
        var beforeForceDeleteCalled = false
        var afterForceDeleteCalled = false
        var findByIdCalled = false
        var deleteCalled = false
        var shouldThrowNotFound = false
        var markAsDeleted = false
        var markAsAlreadyDeleted = false
        val operationOrder = mutableListOf<String>()

        override val repository: Repository<TestEntity, Long> = object : Repository<TestEntity, Long> {}
        
        override var repositorySupport: RepositorySupport = object : RepositorySupport {
            override fun supportIt(repository: Repository<Any, Any>): Boolean = true
            
            override fun <M, ID> persist(model: M, repository: Repository<M, ID>) {
                operationOrder.add("persist")
            }
            
            override fun <M, ID> update(model: M, repository: Repository<M, ID>) {
                operationOrder.add("update")
            }
            
            override fun <M, ID> delete(model: M, repository: Repository<M, ID>) {
                operationOrder.add("delete")
            }
            
            override fun <M, ID> findAll(page: PageRequest, repository: Repository<M, ID>): Page<M> {
                return PageImpl(emptyList())
            }
            
            override fun <M, ID> findById(id: ID, repository: Repository<M, ID>): java.util.Optional<M> {
                throw NotImplementedError()
            }
        }

        override fun findById(id: Long): TestEntity {
            operationOrder.add("findById")
            findByIdCalled = true
            if (shouldThrowNotFound) {
                throw NotFoundException("Entity not found")
            }
            return TestEntity(id, "Test Entity $id").apply {
                if (markAsDeleted) {
                    deletedAt = LocalDateTime.now()
                }
                if (markAsAlreadyDeleted && id == 999L) {
                    deletedAt = LocalDateTime.now()
                }
            }
        }

        override fun update(model: TestEntity, request: TestRequest): TestEntity {
            operationOrder.add("update")
            return model
        }

        override fun delete(model: TestEntity) {
            operationOrder.add("delete")
            deleteCalled = true
        }

        override fun beforeSoftDelete(entity: TestEntity) {
            operationOrder.add("beforeSoftDelete")
            beforeSoftDeleteCalled = true
        }

        override fun afterSoftDelete(entity: TestEntity) {
            operationOrder.add("afterSoftDelete")
            afterSoftDeleteCalled = true
        }

        override fun beforeRestore(entity: TestEntity) {
            operationOrder.add("beforeRestore")
            beforeRestoreCalled = true
        }

        override fun afterRestore(entity: TestEntity) {
            operationOrder.add("afterRestore")
            afterRestoreCalled = true
        }

        override fun beforeForceDelete(entity: TestEntity) {
            operationOrder.add("beforeForceDelete")
            beforeForceDeleteCalled = true
        }

        override fun afterForceDelete(id: Long) {
            operationOrder.add("afterForceDelete")
            afterForceDeleteCalled = true
        }

        override fun store(request: TestRequest): TestEntity {
            throw NotImplementedError()
        }

        override fun index(page: PageRequest, params: Map<String, String>): Page<TestEntity> {
            throw NotImplementedError()
        }

        override fun show(model: TestEntity) = model
    }
}
