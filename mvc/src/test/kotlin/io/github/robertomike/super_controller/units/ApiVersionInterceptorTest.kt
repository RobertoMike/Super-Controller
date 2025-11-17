package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.versioning.ApiVersion
import io.github.robertomike.super_controller.versioning.ApiVersionInterceptor
import io.github.robertomike.super_controller.versioning.VersioningProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.web.method.HandlerMethod
import kotlin.test.assertTrue

class ApiVersionInterceptorTest {

    private lateinit var config: VersioningProperties
    private lateinit var interceptor: ApiVersionInterceptor
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse

    @BeforeEach
    fun setup() {
        config = VersioningProperties(addDeprecationHeaders = true)
        interceptor = ApiVersionInterceptor(config)
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
    }

    @Test
    fun `preHandle should return true for non-HandlerMethod`() {
        val handler = Any()

        val result = interceptor.preHandle(request, response, handler)

        assertTrue(result)
        verifyNoMoreInteractions(response)
    }

    @Test
    fun `preHandle should return true when no ApiVersion annotation`() {
        val handlerMethod = createHandlerMethod(NoVersionController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verifyNoMoreInteractions(response)
    }

    @Test
    fun `preHandle should add version header for versioned controller`() {
        val handlerMethod = createHandlerMethod(VersionedController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
    }

    @Test
    fun `preHandle should add deprecation headers when configured and deprecated`() {
        val handlerMethod = createHandlerMethod(DeprecatedController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        verify(response).addHeader("X-API-Deprecated", "true")
        verify(response).addHeader("Warning", "299 - \"Deprecated API version v1. Please migrate to a newer version.\"")
    }

    @Test
    fun `preHandle should add sunset headers when sunset date specified`() {
        val handlerMethod = createHandlerMethod(DeprecatedWithSunsetController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        verify(response).addHeader("X-API-Deprecated", "true")
        verify(response).addHeader("Sunset", "2025-12-31")
        verify(response).addHeader("X-API-Sunset", "2025-12-31")
        verify(response).addHeader("Warning", "299 - \"Deprecated API version v1. This version will be sunset on 2025-12-31.\"")
    }

    @Test
    fun `preHandle should add deprecation link when documentation URL specified`() {
        val handlerMethod = createHandlerMethod(DeprecatedWithDocsController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        verify(response).addHeader("X-API-Deprecated", "true")
        verify(response).addHeader("Link", "<https://api.example.com/deprecation>; rel=\"deprecation\"")
    }

    @Test
    fun `preHandle should not add deprecation headers when addDeprecationHeaders is false`() {
        config.addDeprecationHeaders = false
        interceptor = ApiVersionInterceptor(config)
        val handlerMethod = createHandlerMethod(DeprecatedController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        // Should not add deprecation headers
        verify(response, never()).addHeader(eq("X-API-Deprecated"), anyString())
    }

    @Test
    fun `preHandle should not add deprecation headers for non-deprecated version`() {
        val handlerMethod = createHandlerMethod(VersionedController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        verify(response, never()).addHeader(eq("X-API-Deprecated"), anyString())
    }

    @Test
    fun `preHandle should add all headers for fully configured deprecated API`() {
        val handlerMethod = createHandlerMethod(FullyDeprecatedController::class.java)

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
        verify(response).addHeader("X-API-Version", "v1")
        verify(response).addHeader("X-API-Deprecated", "true")
        verify(response).addHeader("Sunset", "2025-12-31")
        verify(response).addHeader("X-API-Sunset", "2025-12-31")
        verify(response).addHeader("Link", "<https://api.example.com/v1/deprecation>; rel=\"deprecation\"")
        verify(response).addHeader("Warning", "299 - \"Deprecated API version v1. This version will be sunset on 2025-12-31.\"")
    }

    private fun createHandlerMethod(controllerClass: Class<*>): HandlerMethod {
        val method = controllerClass.methods.first()
        val handlerMethod = mock(HandlerMethod::class.java)
        `when`(handlerMethod.beanType).thenReturn(controllerClass)
        return handlerMethod
    }

    // Test controllers
    private class NoVersionController {
        fun handle() {}
    }

    @ApiVersion("v1")
    private class VersionedController {
        fun handle() {}
    }

    @ApiVersion("v1", deprecated = true)
    private class DeprecatedController {
        fun handle() {}
    }

    @ApiVersion("v1", deprecated = true, sunset = "2025-12-31")
    private class DeprecatedWithSunsetController {
        fun handle() {}
    }

    @ApiVersion("v1", deprecated = true, documentationUrl = "https://api.example.com/deprecation")
    private class DeprecatedWithDocsController {
        fun handle() {}
    }

    @ApiVersion(
        value = "v1",
        deprecated = true,
        sunset = "2025-12-31",
        documentationUrl = "https://api.example.com/v1/deprecation"
    )
    private class FullyDeprecatedController {
        fun handle() {}
    }
}
