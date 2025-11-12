package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.repositories.HibernateRepositorySupport
import io.hypersistence.utils.spring.repository.HibernateRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.Repository
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HibernateRepositorySupportTest {

    private lateinit var support: HibernateRepositorySupport
    private lateinit var mockHibernateRepository: TestHibernateRepository
    private lateinit var mockJpaOnlyRepository: JpaRepository<TestEntity, Long>
    private lateinit var mockNormalRepository: Repository<TestEntity, Long>

    @BeforeEach
    fun setup() {
        support = HibernateRepositorySupport()
        mockHibernateRepository = mock(TestHibernateRepository::class.java)
        mockJpaOnlyRepository = mock(JpaRepository::class.java) as JpaRepository<TestEntity, Long>
        mockNormalRepository = mock(Repository::class.java) as Repository<TestEntity, Long>
    }

    @Test
    fun `supportIt should return true for HibernateRepository and JpaRepository`() {
        val result = support.supportIt(mockHibernateRepository as Repository<Any, Any>)

        assertTrue(result)
    }

    @Test
    fun `supportIt should return false for non-HibernateRepository`() {
        val result = support.supportIt(mockJpaOnlyRepository as Repository<Any, Any>)

        assertFalse(result)
    }

    @Test
    fun `supportIt should return false for non-JpaRepository`() {
        val result = support.supportIt(mockNormalRepository as Repository<Any, Any>)

        assertFalse(result)
    }

    @Test
    fun `findAll should delegate to JpaRepository findAll`() {
        val pageRequest = PageRequest.of(0, 10)
        val expectedPage: Page<TestEntity> = PageImpl(listOf(TestEntity(1L, "Test")))
        `when`(mockHibernateRepository.findAll(pageRequest)).thenReturn(expectedPage)

        val result = support.findAll(pageRequest, mockHibernateRepository)

        assertEquals(expectedPage, result)
        verify(mockHibernateRepository).findAll(pageRequest)
    }

    @Test
    fun `findAll should throw exception for non-JpaRepository`() {
        val pageRequest = PageRequest.of(0, 10)

        val exception = assertThrows<SuperControllerException> {
            support.findAll(pageRequest, mockNormalRepository)
        }

        assertEquals("The repository doesn't extend from JpaRepository", exception.message)
    }

    @Test
    fun `persist should delegate to HibernateRepository persist`() {
        val entity = TestEntity(1L, "Test")

        support.persist(entity, mockHibernateRepository)

        verify(mockHibernateRepository).persist(entity)
    }

    @Test
    fun `persist should throw exception for non-HibernateRepository`() {
        val entity = TestEntity(1L, "Test")

        val exception = assertThrows<SuperControllerException> {
            support.persist(entity, mockJpaOnlyRepository)
        }

        assertEquals("The repository doesn't extend from HibernateRepository", exception.message)
    }

    @Test
    fun `findById should delegate to JpaRepository findById`() {
        val id = 1L
        val expectedEntity = TestEntity(id, "Test")
        `when`(mockHibernateRepository.findById(id)).thenReturn(Optional.of(expectedEntity))

        val result = support.findById(id, mockHibernateRepository)

        assertTrue(result.isPresent)
        assertEquals(expectedEntity, result.get())
        verify(mockHibernateRepository).findById(id)
    }

    @Test
    fun `findById should return empty for non-existent ID`() {
        val id = 999L
        `when`(mockHibernateRepository.findById(id)).thenReturn(Optional.empty())

        val result = support.findById(id, mockHibernateRepository)

        assertFalse(result.isPresent)
    }

    @Test
    fun `update should delegate to HibernateRepository update`() {
        val entity = TestEntity(1L, "Updated")

        support.update(entity, mockHibernateRepository)

        verify(mockHibernateRepository).update(entity)
    }

    @Test
    fun `update should throw exception for non-HibernateRepository`() {
        val entity = TestEntity(1L, "Updated")

        val exception = assertThrows<SuperControllerException> {
            support.update(entity, mockJpaOnlyRepository)
        }

        assertEquals("The repository doesn't extend from HibernateRepository", exception.message)
    }

    @Test
    fun `delete should delegate to JpaRepository delete`() {
        val entity = TestEntity(1L, "Test")

        support.delete(entity, mockHibernateRepository)

        verify(mockHibernateRepository).delete(entity)
    }

    @Test
    fun `delete should throw exception for non-JpaRepository`() {
        val entity = TestEntity(1L, "Test")

        val exception = assertThrows<SuperControllerException> {
            support.delete(entity, mockNormalRepository)
        }

        assertEquals("The repository doesn't extend from JpaRepository", exception.message)
    }

    // Test entity
    private data class TestEntity(val id: Long, val name: String)

    // Test repository interface that extends both HibernateRepository and JpaRepository
    private interface TestHibernateRepository : HibernateRepository<TestEntity>, JpaRepository<TestEntity, Long>
}
