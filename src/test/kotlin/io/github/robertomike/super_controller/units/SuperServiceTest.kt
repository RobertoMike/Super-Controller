package io.github.robertomike.super_controller.units;

import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.requests.orders.StoreOrderRequest
import io.github.robertomike.super_controller.examples.services.OrderService
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.services.SuperService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.modelmapper.ModelMapper
import org.springframework.data.repository.Repository
import java.util.*

@ExtendWith(MockitoExtension::class)
class SuperServiceTest {
    private lateinit var orderRepository: OrderRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: OrderService

    @BeforeEach
    fun init() {
        orderRepository = mock(OrderRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        service = OrderService(orderRepository, userRepository)
        service.modelMapper = ModelMapper()
    }

    @Test
    fun customs_errors() {
        service.useCustomDelete = true
        service.useCustomSave = true
        service.useCustomFindById = true
        `when`(userRepository.findById(any())).thenReturn(Optional.of(User()))
        `when`(orderRepository.findById(any())).thenReturn(Optional.of(Order()))

        assertThrows<SuperControllerException> { service.show(1L) }
        assertThrows<SuperControllerException> { service.store(StoreOrderRequest(userId = 1)) }

        service.useCustomFindById = false

        assertThrows<SuperControllerException> { service.delete(1L) }
    }

    @Test
    fun customsFunctions() {
        val temporal = object : SuperService<Order, Long>() {
            override fun config() {
                useCustomDelete = true
                useCustomSave = true
                useCustomFindById = true
            }

            override val repository: Repository<Order, Long>
                get() = orderRepository

            override fun customSave(model: Order) {}
            override fun customDelete(model: Order) {}
            override fun customFindById(id: Long): Optional<Order> {
                return Optional.of(Order(id = id))
            }
        }

        temporal.modelMapper = ModelMapper()
        temporal.config()

        assertDoesNotThrow {
            temporal.show(1L)
            temporal.store(StoreOrderRequest())
            temporal.delete(1L)
        }
    }

    @Test
    fun errorRepositoryCustomNotImplementedAndIsSimpleRepository() {
        val temporal = object : SuperService<Order, Long>() {
            override val repository: Repository<Order, Long>
                get() = mock(Repository::class.java) as Repository<Order, Long>

            override fun customFindById(id: Long): Optional<Order> {
                return Optional.of(Order(id = id))
            }
        }

        temporal.modelMapper = ModelMapper()

        assertThrows<SuperControllerException> {
            temporal.show(1L)
        }

        assertThrows<SuperControllerException> {
            temporal.store(StoreOrderRequest())
        }

        temporal.useCustomFindById = true

        assertThrows<SuperControllerException> {
            temporal.delete(1L)
        }
    }

    @Test
    fun searchModel_modelNotFound() {
        val temporal = object : SuperService<Order, Long>() {
            override val repository: Repository<Order, Long>
                get() = orderRepository
        }

        `when`(orderRepository.findById(any())).thenReturn(Optional.empty())

        assertThrows<NotFoundException> {
            temporal.show(1L)
        }
    }

    @Test
    fun repositoryNotImplemented() {
        val temporal = object : SuperService<Order, Long>() {}

        assertThrows<SuperControllerException> {
            temporal.show(1L)
        }
    }
}
