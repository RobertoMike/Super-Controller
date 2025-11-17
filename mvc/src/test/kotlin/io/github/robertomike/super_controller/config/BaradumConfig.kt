package io.github.robertomike.super_controller.config

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.core.requests.BasicRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import java.util.stream.Collectors

@Configuration
open class BaradumConfig(request: HttpServletRequest) {
    init {
        Baradum.request = ApacheTomcatRequest(request)
    }
}

class ApacheTomcatRequest(request: HttpServletRequest) : BasicRequest<HttpServletRequest>(request) {
    override fun findParamByName(name: String): String? {
        return request.getParameter(name)
    }

    override val method: String
        get() = request.method

    override val json: String
        get() = request.reader.lines().collect(Collectors.joining(System.lineSeparator()))
}