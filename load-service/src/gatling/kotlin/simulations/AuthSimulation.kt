package simulations

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import java.time.Duration

class AuthSimulation : Simulation() {

    private val register = exec(
        http("register")
            .post("/auth/register")
            .body(StringBody(
                """
                {
                  "name": "Oleg Shipulin #{randomInt}",
                  "email": "shipOS#{randomInt}@founder.com",
                  "password": "password123"
                }
                """.trimIndent()
            ))
            .asJson()
            .check(status().`in`(200, 409))
            .check(jsonPath("$.token").saveAs("jwt"))
            .check(jsonPath("$.user.id").saveAs("userId"))
            .check(jsonPath("$.user.name").saveAs("userName"))
    )

    private val login = exec(
        http("login")
            .post("/auth/login")
            .body(StringBody(
                """
                {
                  "email": "shipOS#{randomInt}@founder.com",
                  "password": "password123"
                }
                """.trimIndent()
            ))
            .asJson()
            .check(status().`is`(200))
            .check(jsonPath("$.token").saveAs("jwt"))
    )

    private val verify = exec(
        http("verify")
            .get("/auth/verify")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().`is`(200))
    )

    private val scn = scenario("Auth load")
        .exec { session ->
            session.set("randomInt", (0..100000).random())
        }
        .exec(register)
        .exitHereIfFailed()

    init {
        setUp(
            scn.injectOpen(
                rampUsers(10_000).during(Duration.ofSeconds(60))
            )
        ).protocols(BaseHttp.httpProtocol)
    }
}
