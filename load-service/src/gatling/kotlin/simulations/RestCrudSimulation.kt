package simulations

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import java.time.*

class RestCrudSimulation : Simulation() {

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

        private val createRoom = exec(
        http("create room")
            .post("/rooms")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("""{"name": "Test Room #{randomInt}"}"""))
            .asJson()
            .check(status().`in`(200, 201))
            .check(jsonPath("$.id").saveAs("roomId"))
    )

    private val getRooms = exec(
        http("get rooms")
            .get("/rooms")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().`is`(200))
            .check(jsonPath("$[0].id").saveAs("roomId"))
    )

    private val postMessage = exec(
        http("post message")
            .post("/messages")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody(
                """
                {
                  "author": {
                    "id": #{userId},
                    "name": "#{userName}"
                  }
                  "room": #{roomId},
                  "created": "#{timestamp}",
                  "text": "hello from gatling #{randomInt}"
                }
                """.trimIndent()
            ))
            .asJson()
            .check(status().`in`(200, 201))
    )

    private val scn = scenario("REST CRUD load")
        .exec { session ->
            session.set("randomInt", (0..100000).random())
            .set("timestamp", java.time.Instant.now().toString())
        }
        .exec(register)
        .exitHereIfFailed()
        .pause(Duration.ofMillis(300))
        .exec(createRoom)
        .exitHereIfFailed()
        .exec(getRooms)
        .exitHereIfFailed()
        .pause(Duration.ofMillis(300))
        .repeat(1).on(
            exec(postMessage).pause(Duration.ofMillis(300))
        )

    init {
        setUp(
            scn.injectOpen(
                rampUsers(10_000).during(Duration.ofSeconds(60))
            )
        ).protocols(BaseHttp.httpProtocol)
    }
}
