/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.controllers

import org.mockito.ArgumentMatchers.{ any, eq as eqTo }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.*
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.auth.core.{ AuthConnector, BearerTokenExpired }
import uk.gov.hmrc.digitalcontactapi.connectors.SecureMessageConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SecureMessageControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "SecureMessageController GET /secure-messaging/messages/count" should {
    "delegate to SecureMessageConnector and return the result" in new Setup {
      val mockResult: Result = Results.Ok("""{"message":"ok"}""").as("application/json")
      when(mockConnector.forwardGetRequest(eqTo("/secure-messaging/messages/count"))(any, any))
        .thenReturn(Future.successful(mockResult))
      when(mockAuthConnector.authorise[Any](any(), any())(any(), any())).thenReturn(Future.successful {})
      val result = controller.messagesCount.apply(FakeRequest())
      status(result) shouldBe OK
    }
  }
  "return UNAUTHORIZED when the auth connector throws an AuthorisationException (Covers Lines 30-32)" in new Setup {
    // Mock failed auth (BearerTokenExpired is a subclass of AuthorisationException)
    when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
      .thenReturn(Future.failed(BearerTokenExpired()))

    val result = controller.messagesCount.apply(FakeRequest())

    status(result) shouldBe UNAUTHORIZED
  }

  trait Setup {
    val mockConnector: SecureMessageConnector = mock[SecureMessageConnector]
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val controller = new SecureMessageController(mockAuthConnector, Helpers.stubControllerComponents(), mockConnector)
  }

}
