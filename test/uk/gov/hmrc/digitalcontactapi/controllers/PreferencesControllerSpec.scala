/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.controllers

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.test.FakeRequest
import play.api.mvc.ControllerComponents
import play.api.Configuration
import play.api.libs.json.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.digitalcontactapi.connectors.PreferencesConnector
import uk.gov.hmrc.digitalcontactapi.model.*
import uk.gov.hmrc.digitalcontactapi.model.StatusName.*

import scala.concurrent.{ ExecutionContext, Future }

class PreferencesControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "PreferencesController.preferences" should {
    "check bad request response" in new SetUp {
      val response = Json.obj("reason" -> "bad request", "code" -> 123)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            HttpResponse(
              status = BAD_REQUEST,
              body = response.toString(),
              headers = Map.empty
            )
          )
        )

      val result = controller.preferences().apply(request)

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe response
    }

    "recover with 500 and an error JSON if the connector fails" in new SetUp {
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Failed to retrieve preferences")))

      val result = controller.preferences().apply(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve preferences"
    }

    "check for Alright status" in new SetUp {
      val preferences = makePreferencesJson(StatusNameResponse.Alright)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)

      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe true
      statusName(responseJson) mustBe Alright.status
      redirectUrl(responseJson) mustBe controller.checkSettingsUrl(hostContext)
    }

    "check for NewCustomer status when no preferences found" in new SetUp {
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "", Map.empty)))

      val result = controller.preferences().apply(request)

      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe NewCustomer.status
      redirectUrl(responseJson) mustBe controller.optInUrl(hostContext)
    }

    "check for NewCustomer status " in new SetUp {
      val preferences = makePreferencesJson(StatusNameResponse.NewCustomer)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)

      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe NewCustomer.status
      redirectUrl(responseJson) mustBe controller.optInUrl(hostContext)
    }

    "check for NoEmail status" in new SetUp {
      val preferences = makePreferencesJson(StatusNameResponse.NoEmail)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe NoEmail.status
      redirectUrl(responseJson) mustBe controller.checkSettingsUrl(hostContext)
    }

    "check for Paper status" in new SetUp {
      val preferences = makePreferencesJson(StatusNameResponse.Paper)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe Paper.status
      redirectUrl(responseJson) mustBe controller.checkSettingsUrl(hostContext)
    }

    "check for EmailNotVerified status" in new SetUp {
      val email = Some("user@example.com")
      val preferences = makePreferencesJson(StatusNameResponse.EmailNotVerified, email)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe EmailNotVerified.status
      redirectUrl(responseJson) mustBe controller.emailReVerifyUrl(hostContext.copy(email = email))
    }

    "check for BouncedEmail status" in new SetUp {
      val email = Some("user@example.com")
      val preferences = makePreferencesJson(StatusNameResponse.BouncedEmail, email)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe false
      statusName(responseJson) mustBe BouncedEmail.status
      redirectUrl(responseJson) mustBe controller.bounceUrl(hostContext.copy(email = email))
    }

    "check for ReOptIn status" in new SetUp {
      val email = Some("user@example.com")
      val preferences = makePreferencesJson(StatusNameResponse.OldVersion, email)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe true
      statusName(responseJson) mustBe ReOptIn.status
      val expectedHost = hostContext.copy(email = email, cohort = Some(ReOptInCohort))
      redirectUrl(responseJson) mustBe controller.reOptInUrl(ReOptInCohort, expectedHost)
    }

    "check for ReOptInModified status" in new SetUp {
      val email = Some("user@example.com")
      val preferences = makePreferencesJson(StatusNameResponse.ReOptInModified, email)
      when(mockConnector.getPreferences()(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, preferences.toString(), Map.empty)))

      val result = controller.preferences().apply(request)
      status(result) mustBe OK
      val responseJson = contentAsJson(result)

      isDigital(responseJson) mustBe true
      statusName(responseJson) mustBe ReOptIn.status
      val expectedHost = hostContext.copy(email = email, cohort = Some(ReOptInCohort))
      redirectUrl(responseJson) mustBe controller.reOptInUrl(ReOptInCohort, expectedHost)
    }
  }

  trait SetUp {
    given ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    given hc: HeaderCarrier = HeaderCarrier()

    given queryParams: QueryParams = QueryParams(returnUrl = "test-url", returnLinkText = "test-linkText")

    val hostContext: HostContext = HostContext(queryParams.returnUrl, queryParams.returnLinkText)

    lazy val app = new GuiceApplicationBuilder().build()

    val cc: ControllerComponents = stubControllerComponents()
    val mockConnector = mock[PreferencesConnector]
    val mockAuthConnector = mock[AuthConnector]
    val config: Configuration = app.configuration
    val controller = new PreferencesController(cc, mockConnector, mockAuthConnector, config)

    when(mockAuthConnector.authorise[Any](any(), any())(any(), any())).thenReturn(Future.successful {})

    val request = FakeRequest("GET", "/preferences")

    def makePreferencesJson(statusName: StatusNameResponse, email: Option[String] = None): JsObject = {
      val statusJson = Json.obj("status" -> Json.obj("name" -> statusName))
      val emailJson = email.fold(Json.obj())(e => Json.obj("email" -> Json.obj("email" -> e)))
      emailJson ++ statusJson
    }

    def statusName(js: JsValue): String =
      (js \ "status")
        .asOpt[String]
        .orElse((js \ "status" \ "name").asOpt[String])
        .getOrElse(sys.error(s"Could not extract status name from JSON: $js"))

    def isDigital(js: JsValue): Boolean =
      (js \ "isDigital")
        .asOpt[Boolean]
        .orElse((js \ "digital").asOpt[Boolean])
        .getOrElse(sys.error(s"Could not extract digital flag from JSON: $js"))

    def redirectUrl(js: JsValue): String = (js \ "redirectUrl").as[String]

    val ReOptInCohort = 55

  }

}
