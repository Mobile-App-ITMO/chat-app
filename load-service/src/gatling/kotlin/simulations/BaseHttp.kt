package simulations

import io.gatling.javaapi.http.HttpDsl.*

object BaseHttp {
    val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .shareConnections()
}
