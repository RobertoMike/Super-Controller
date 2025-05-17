package io.github.robertomike.super_controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.reactive.utils.Page
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.ReactiveTransactionManager
import java.util.function.Consumer

@EnableR2dbcRepositories
@EnableR2dbcAuditing
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureWebTestClient
open class BasicTest {
    @Autowired
    lateinit var webTestClient: WebTestClient
    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate
    @Autowired
    private lateinit var reactiveTransactionManager: ReactiveTransactionManager
    private var mapper: ObjectMapper = ObjectMapper()

    @PostConstruct
    fun init() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun <T> makeIndex(
        url: String,
        elementAction: Consumer<T>?,
        pageAction: Consumer<Page<T>>?,
        typeReference: TypeReference<Page<T>>
    ) {
        webTestClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { result ->
                val page = mapper.readValue(
                    result.responseBody,
                    typeReference
                )
                pageAction?.accept(page)
                if (elementAction != null) {
                    page.content.forEach(elementAction)
                }
            }
    }

    fun <T> makeShow(
        url: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>?
    ) {
        webTestClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { result ->
                val element: T = mapper.readValue(
                    result.responseBody,
                    typeReference
                )
                elementAction?.accept(element)
            }
    }

    fun <T> makeStore(
        url: String,
        body: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>
    ) {
        basicCall(HttpMethod.POST, url, body, typeReference, elementAction) { it.isCreated }
    }

    fun <T> makePut(
        url: String,
        body: String,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>
    ) {
        basicCall(HttpMethod.PUT, url, body, typeReference, elementAction) { it.isOk }
    }

    fun <T> basicCall(
        requestMethod: HttpMethod,
        url: String,
        body: String?,
        typeReference: TypeReference<T>,
        elementAction: Consumer<T>?,
        statusAssertion: (el: StatusAssertions) -> WebTestClient.ResponseSpec
    ) {
        val requestBuilder = webTestClient.method(requestMethod)
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            requestBuilder.bodyValue(body)
        }

        val exchange = requestBuilder.exchange()

        statusAssertion(exchange.expectStatus())
            .expectBody()
            .consumeWith { result ->
                val element = mapper.readValue(
                    result.responseBody,
                    typeReference
                )
                elementAction?.accept(element)
            }
    }

    fun <T> makeIndex(
        url: String,
        elementAction: Consumer<T>?,
        typeReference: TypeReference<Page<T>>
    ) {
        makeIndex(url, elementAction, null, typeReference)
    }

    fun <T> makeIndex(
        url: String,
        typeReference: TypeReference<Page<T>>
    ) {
        makeIndex(url, null, null, typeReference)
    }

    fun makeDelete(url: String) {
        webTestClient.delete()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }
}
