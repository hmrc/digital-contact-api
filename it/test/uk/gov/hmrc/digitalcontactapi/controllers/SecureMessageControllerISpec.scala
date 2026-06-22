/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.controllers

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.WSClient
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

class SecureMessageControllerISpec extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with WireMockSupport {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.secure-message.host" -> "localhost",
        "microservice.services.secure-message.port" -> wireMockPort,
        "microservice.services.auth.host"           -> "localhost",
        "microservice.services.auth.port"           -> wireMockPort
      )
      .build()

  "GET /secure-messaging/messages/count" should {

    "return 200 and response from Secure Message Service when authorised" in {
      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("{}")
          )
      )

      stubFor(
        get(urlEqualTo("/secure-messaging/messages/count"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "count": {
                  |    "total": 42,
                  |    "unread": 4
                  |  }
                  |}""".stripMargin
              )
          )
      )

      val response = await(
        wsClient
          .url(s"http://localhost:$port/messages/count")
          .addHttpHeaders("Authorization" -> "Bearer it-test-token")
          .get()
      )

      response.status shouldBe OK
      val json: JsValue = Json.parse(response.body)
      (json \ "count" \ "total").as[Int] shouldBe 42
      (json \ "count" \ "unread").as[Int] shouldBe 4

      verify(getRequestedFor(urlEqualTo("/secure-messaging/messages/count")))
    }

    "return 401 when user is unauthorised" in {
      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(aResponse().withStatus(UNAUTHORIZED))
      )

      val response = await(
        wsClient
          .url(s"http://localhost:$port/messages/count")
          .get()
      )

      response.status shouldBe UNAUTHORIZED

      verify(0, getRequestedFor(urlEqualTo("/secure-messaging/messages/count")))
    }
  }
}
