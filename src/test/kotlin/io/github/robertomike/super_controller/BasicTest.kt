package io.github.robertomike.super_controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.utils.Page
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureMockMvc
@Transactional
open class BasicTest {
    @Autowired
    lateinit var mockMvc: MockMvc
    private var mapper: ObjectMapper = ObjectMapper()

    @PostConstruct
    fun init() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Throws(Exception::class)
    fun <T> makeIndex(
        url: String,
        elementAction: Consumer<T>?,
        pageAction: Consumer<Page<T>>?,
        typeReference: TypeReference<Page<T>>
    ) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect { result ->
                val page = mapper.readValue(
                    result
                        .response
                        .contentAsString,
                    typeReference
                )
                pageAction?.accept(page)
                if (elementAction != null) {
                    page.content.forEach(elementAction)
                }
            }
    }

    @Throws(Exception::class)
    fun <T> makeShow(
        url: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>?
    ) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect { result ->
                val element: T = mapper.readValue(
                    result.response.contentAsString,
                    typeReference
                )
                elementAction?.accept(element)
            }
    }

    @Throws(Exception::class)
    fun <T> makeStore(
        url: String,
        body: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>
    ) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
        basicCall(request, body, typeReference, elementAction, MockMvcResultMatchers.status().isCreated())
    }

    @Throws(Exception::class)
    fun <T> makePut(
        url: String,
        body: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>
    ) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put(url)
        basicCall(request, body, typeReference, elementAction, MockMvcResultMatchers.status().isOk())
    }

    @Throws(Exception::class)
    fun <T> basicCall(
        request: MockHttpServletRequestBuilder,
        body: String?,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>?,
        status: ResultMatcher
    ) {
        request.contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            request.content(body)
        }

        mockMvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status)
            .andExpect { result ->
                val element = mapper.readValue(
                    result.response.contentAsString,
                    typeReference
                )
                elementAction?.accept(element)
            }
    }

    @Throws(Exception::class)
    fun <T> makeIndex(
        url: String,
        elementAction: Consumer<T>?,
        typeReference: TypeReference<Page<T>>
    ) {
        makeIndex(url, elementAction, null, typeReference)
    }

    @Throws(Exception::class)
    fun <T> makeIndex(
        url: String,
        typeReference: TypeReference<Page<T>>
    ) {
        makeIndex(url, null, null, typeReference)
    }


    @Throws(Exception::class)
    fun makeDelete(url: String) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(url)
            .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }
}
