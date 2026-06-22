/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.backend.http.JsonErrorHandler

import scala.concurrent.{ ExecutionContext, Future }

class ErrorHandlerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  val mockJsonErrorHandler: JsonErrorHandler = mock[JsonErrorHandler]
  val handler = new ErrorHandler(mockJsonErrorHandler)

  "ErrorHandler.onClientError" should {

    "return BAD_REQUEST (400) with JSON containing the message" in {

      val request: RequestHeader = FakeRequest("GET", "/test")
      val message = "Missing required query parameter: returnUrl"

      val result: Future[Result] = handler.onClientError(request, BAD_REQUEST, message)

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe Json.obj("error" -> message)

      verify(mockJsonErrorHandler, times(1)).onClientError(eqTo(request), eqTo(BAD_REQUEST), eqTo(message))
    }

    "delegate to JsonErrorHandler for non-400 errors" in {

      val request: RequestHeader = FakeRequest("GET", "/test")

      handler.onClientError(request, NOT_FOUND, "not found")

      verify(mockJsonErrorHandler, times(1)).onClientError(eqTo(request), eqTo(NOT_FOUND), eqTo("not found"))
    }
  }

  "ErrorHandler.onServerError" should {

    "delegate to JsonErrorHandler" in {
      val request: RequestHeader = FakeRequest("GET", "/server-error")
      val ex = new RuntimeException("boom")

      handler.onServerError(request, ex)

      verify(mockJsonErrorHandler, times(1)).onServerError(eqTo(request), eqTo(ex))
    }
  }
}
