package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.config.router.BaseRouter
import io.github.robertomike.super_controller.controllers.CrudController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.RequestMethod
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BaseRouterTest {

    @Test
    fun `searchMethodFor should find existing method by name`() {
        val controller = TestController()
        val router = TestRouterImpl(listOf(controller))
        
        val method = router.searchMethodFor(controller, "index")
        
        assertNotNull(method)
        assertEquals("index", method.name)
    }

    @Test
    fun `searchMethodFor should throw exception for non-existent method`() {
        val controller = TestController()
        val router = TestRouterImpl(listOf(controller))
        
        val exception = assertThrows<RuntimeException> {
            router.searchMethodFor(controller, "nonExistentMethod")
        }
        
        assertTrue(exception.message?.contains("Cannot find method") == true)
    }

    @Test
    fun `registerCrud should register all specified CRUD methods`() {
        val controller = TestController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<TestController>(listOf(controller)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerCrud(controller, "/api/test", listOf(Methods.INDEX, Methods.STORE, Methods.SHOW, Methods.UPDATE, Methods.DESTROY))
        
        assertEquals(5, registeredUrls.size)
        assertTrue(registeredUrls.any { it.method == "index" && it.url == "/api/test" && it.httpMethod == RequestMethod.GET })
        assertTrue(registeredUrls.any { it.method == "store" && it.url == "/api/test" && it.httpMethod == RequestMethod.POST })
        assertTrue(registeredUrls.any { it.method == "show" && it.url == "/api/test/{id}" && it.httpMethod == RequestMethod.GET })
        assertTrue(registeredUrls.any { it.method == "update" && it.url == "/api/test/{id}" && it.httpMethod == RequestMethod.PUT })
        assertTrue(registeredUrls.any { it.method == "destroy" && it.url == "/api/test/{id}" && it.httpMethod == RequestMethod.DELETE })
    }

    @Test
    fun `registerCrud should only register specified methods`() {
        val controller = TestController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<TestController>(listOf(controller)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerCrud(controller, "/api/test", listOf(Methods.INDEX, Methods.STORE))
        
        assertEquals(2, registeredUrls.size)
        assertTrue(registeredUrls.all { it.method in listOf("index", "store") })
    }

    @Test
    fun `registerAll should register CRUD routes for controller`() {
        val controller = TestController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<TestController>(listOf(controller)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerAll()
        
        // Should have registered at least the INDEX method from TestController
        assertTrue(registeredUrls.isNotEmpty())
        assertTrue(registeredUrls.any { it.method == "index" })
    }

    @Test
    fun `registerAll should detect and register BulkOperations routes`() {
        val controller = TestBulkController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<CrudController<*, *, *, *, *, *>>(listOf(controller)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerAll()
        
        // Should have registered bulk operations
        assertTrue(registeredUrls.any { it.method == "bulkStore" && it.url == "/api/users/bulk" && it.httpMethod == RequestMethod.POST })
        assertTrue(registeredUrls.any { it.method == "bulkUpdate" && it.url == "/api/users/bulk" && it.httpMethod == RequestMethod.PUT })
        assertTrue(registeredUrls.any { it.method == "bulkDelete" && it.url == "/api/users/bulk" && it.httpMethod == RequestMethod.DELETE })
    }

    @Test
    fun `registerAll should detect and register SoftDeletable routes`() {
        val controller = TestSoftDeleteController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<CrudController<*, *, *, *, *, *>>(listOf(controller)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerAll()
        
        // Should have registered soft delete operations
        assertTrue(registeredUrls.any { it.method == "softDelete" && it.url == "/api/users/{id}/soft-delete" && it.httpMethod == RequestMethod.DELETE })
        assertTrue(registeredUrls.any { it.method == "restore" && it.url == "/api/users/{id}/restore" && it.httpMethod == RequestMethod.PUT })
        assertTrue(registeredUrls.any { it.method == "forceDelete" && it.url == "/api/users/{id}/force" && it.httpMethod == RequestMethod.DELETE })
    }

    @Test
    fun `registerAll should handle multiple controllers`() {
        val controller1 = TestController()
        val controller2 = TestController()
        val registeredUrls = mutableListOf<UrlRegistration>()
        val router = object : BaseRouter<TestController>(listOf(controller1, controller2)) {
            override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
                registeredUrls.add(UrlRegistration(method, url, httpMethod))
            }
        }
        
        router.registerAll()
        
        // Should have registered routes for both controllers
        assertTrue(registeredUrls.size >= 2) // At least 2 index methods
    }

    // Test helper classes
    private data class UrlRegistration(val method: String, val url: String, val httpMethod: RequestMethod)

    private class TestRouterImpl(controllers: List<TestController>) : BaseRouter<TestController>(controllers) {
        override fun registerUrl(controller: Any, method: String, url: String, httpMethod: RequestMethod) {
            // No-op for testing
        }
    }

    private open class TestController : CrudController<Long, Any, Request, Request, Page<*>, Unit> {
        override val baseUrl = "/api/test"
        override val urls = listOf(Methods.INDEX)
        
        override fun index(page: Int, size: Int, params: Map<String, String>): Page<*> {
            return PageImpl(emptyList<Any>())
        }
        
        override fun store(request: Request) = Any()
        override fun show(id: Long) = Any()
        override fun update(id: Long, request: Request) = Any()
        override fun destroy(id: Long) {}
    }

    private class TestBulkController : CrudController<Long, Any, Request, Request, Page<*>, Unit>,
        BulkOperationsMarker<User, Long, Request, Request> {
        override val baseUrl = "/api/users"
        override val urls = listOf(Methods.INDEX)
        override lateinit var service: BasicService<User, Page<User>, Long, Request, Request, Unit>
        
        override fun index(page: Int, size: Int, params: Map<String, String>) = PageImpl(emptyList<Any>())
        override fun store(request: Request) = Any()
        override fun show(id: Long) = Any()
        override fun update(id: Long, request: Request) = Any()
        override fun destroy(id: Long) {}
    }

    private class TestSoftDeleteController : CrudController<Long, Any, Request, Request, Page<*>, Unit>,
        SoftDeletableMarker<User, Long> {
        override val baseUrl = "/api/users"
        override val urls = listOf(Methods.INDEX)
        override lateinit var service: BasicService<User, Page<User>, Long, Request, Request, Unit>
        
        override fun index(page: Int, size: Int, params: Map<String, String>) = PageImpl(emptyList<Any>())
        override fun store(request: Request) = Any()
        override fun show(id: Long) = Any()
        override fun update(id: Long, request: Request) = Any()
        override fun destroy(id: Long) {}
    }
}
