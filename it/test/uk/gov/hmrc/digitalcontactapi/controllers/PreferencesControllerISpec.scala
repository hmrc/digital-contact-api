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
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import scala.concurrent.Await
import scala.concurrent.duration.*

class PreferencesControllerISpec extends AnyWordSpec with Matchers with WireMockSupport with GuiceOneServerPerSuite {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.preferences.host" -> "localhost",
        "microservice.services.preferences.port" -> wireMockPort,
        "microservice.services.auth.host"        -> "localhost",
        "microservice.services.auth.port"        -> wireMockPort
      )
      .build()

  "GET /paperless/preferences" should {

    "return 200 (OK) with digital preference when preferences exist" in {

      val responseBody =
        """{
          |  "email": { "email": "test@example.com" },
          |  "status": { "name": "ALRIGHT" }
          |}""".stripMargin

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
        get(urlMatching(".*/preferences"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val queryParams = "?returnUrl=http%3A%2F%2Flocalhost&returnLinkText=Back"

      val response = Await.result(
        wsClient
          .url(s"http://localhost:$port/paperless/preferences$queryParams")
          .addHttpHeaders("Authorization" -> "Bearer it-test-token")
          .get(),
        5.seconds
      )

      response.status shouldBe OK

      val json: JsValue = Json.parse(response.body)
      (json \ "digital").as[Boolean] shouldBe true
      (json \ "status").as[String] shouldBe "ALRIGHT"
      (json \ "redirectUrl").as[String] should include("/paperless/check-settings")

      verify(getRequestedFor(urlMatching(".*/preferences")))
    }

    "return 200 (OK) with non-digital status when preferences are not found" in {

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
        get(urlEqualTo("/preferences"))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val queryParams = "?returnUrl=http%3A%2F%2Flocalhost&returnLinkText=Back"

      val response = Await.result(
        wsClient
          .url(s"http://localhost:$port/paperless/preferences$queryParams")
          .addHttpHeaders("Authorization" -> "Bearer it-test-token")
          .get(),
        5.seconds
      )

      response.status shouldBe OK
      val json = Json.parse(response.body)
      (json \ "digital").as[Boolean] shouldBe false
    }

    "return 500 (INTERNAL_SERVER_ERROR) when connector fails" in {

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
        get(urlMatching(".*/preferences.*"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val queryParams = "?returnUrl=http%3A%2F%2Flocalhost&returnLinkText=Back"

      val response = Await.result(
        wsClient
          .url(s"http://localhost:$port/paperless/preferences$queryParams")
          .addHttpHeaders("Authorization" -> "Bearer it-test-token")
          .get(),
        5.seconds
      )

      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 401 (UNAUTHORIZED) when user is not authorised" in {
      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(aResponse().withStatus(UNAUTHORIZED))
      )

      val queryParams = "?returnUrl=a&returnLinkText="

      val response = Await.result(
        wsClient
          .url(s"http://localhost:$port/paperless/preferences$queryParams")
          .addHttpHeaders("Authorization" -> "Bearer it-test-token")
          .get(),
        5.seconds
      )

      response.status shouldBe UNAUTHORIZED
      verify(0, getRequestedFor(urlMatching(".*/preferences.*")))
    }
  }
}
