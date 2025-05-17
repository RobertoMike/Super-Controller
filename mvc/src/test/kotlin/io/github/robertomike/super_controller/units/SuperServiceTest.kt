package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.examples.mappers.OrderMapper
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.services.OrderService
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.SuperService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.Repository
import java.util.*

@ExtendWith(MockitoExtension::class)
class SuperServiceTest {
    private lateinit var orderRepository: OrderRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: OrderService
    private lateinit var mapper: OrderMapper

    @BeforeEach
    fun init() {
        orderRepository = mock(OrderRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        mapper = mock(OrderMapper::class.java)
        service = OrderService(orderRepository, userRepository, mapper)
    }


    @Test
    fun searchModel_modelNotFound() {
        val temporal = object : SuperService<Order, Long, Request, Request>() {
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
        val temporal = object : SuperService<Order, Long, Request, Request>() {}

        assertThrows<SuperControllerException> {
            temporal.show(1L)
        }
    }
}
