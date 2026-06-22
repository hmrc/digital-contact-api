/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.QueryStringBindable

class QueryBindableSupportSpec extends AnyWordSpec with Matchers {

  given QueryStringBindable[String] = QueryStringBindable.bindableString

  val queryParamsBinder: QueryStringBindable[QueryParams] = QueryParams.queryStringBindable
  val hostContextBinder: QueryStringBindable[HostContext] = HostContext.queryStringBindable

  "QueryParams.queryStringBindable" when {

    "binding" should {

      "successfully bind when all required parameters are present" in {
        val params = Map(
          "returnUrl"      -> Seq("https%3A%2F%2Fwww.example.com%2Ftest"),
          "returnLinkText" -> Seq("Go back to service")
        )
        val expected = QueryParams("https://www.example.com/test", "Go back to service")

        queryParamsBinder.bind("key", params) shouldBe Some(Right(expected))
      }

      "fail to bind if 'returnUrl' is missing" in {
        val params = Map(
          "returnLinkText" -> Seq("Go back to service")
        )
        val expectedError = "Missing required query parameter(s): returnUrl"

        queryParamsBinder.bind("key", params) shouldBe Some(Left(expectedError))
      }

      "fail to bind if 'returnLinkText' is missing" in {
        val params = Map(
          "returnUrl" -> Seq("https://www.example.com/test")
        )
        val expectedError = "Missing required query parameter(s): returnLinkText"

        queryParamsBinder.bind("key", params) shouldBe Some(Left(expectedError))
      }

      "fail to bind and report all missing parameters" in {
        val params = Map.empty[String, Seq[String]]
        val result = queryParamsBinder.bind("key", params)

        result shouldBe a[Some[Left[_, _]]]
        val error = result.get.swap.getOrElse("")
        error should include("Missing required query parameter(s): returnUrl, returnLinkText")
      }
    }

    "unbinding" should {

      "unbind a QueryParams instance into an encrypted query string" in {
        val queryParams = QueryParams("https://www.example.com/test", "Go back")
        val result = queryParamsBinder.unbind("key", queryParams)

        val expectedPart1 = "returnUrl=Z88PydbLo24LElXdxb5xpj2Y9WqqqL6PPMzZxEL3RcQ%253D"
        val expectedPart2 = "returnLinkText=jxygc4vc6SWJACOpTObcrQ%253D%253D"

        result should include(expectedPart1)
        result should include(expectedPart2)
        result should include("&")
      }
    }
  }

  "HostContext.queryStringBindable" when {

    "binding" should {

      "successfully bind when only required parameters are present" in {
        val params = Map(
          "returnUrl"      -> Seq("https://www.example.com/test"),
          "returnLinkText" -> Seq("Go back to service")
        )
        val expected = HostContext("https://www.example.com/test", "Go back to service")

        hostContextBinder.bind("key", params) shouldBe Some(Right(expected))
      }

      "successfully bind when all parameters are present" in {
        val params = Map(
          "returnUrl"      -> Seq("https://www.example.com/test"),
          "returnLinkText" -> Seq("Go back to service"),
          "email"          -> Seq("test@example.com"),
          "cohort"         -> Seq("123")
        )
        val expected =
          HostContext("https://www.example.com/test", "Go back to service", Some(123), Some("test@example.com"))

        hostContextBinder.bind("key", params) shouldBe Some(Right(expected))
      }

      "successfully bind and set cohort to None if it's not a valid integer" in {
        val params = Map(
          "returnUrl"      -> Seq("https://www.example.com/return"),
          "returnLinkText" -> Seq("Go back"),
          "cohort"         -> Seq("not-a-number")
        )
        val expected = HostContext("https://www.example.com/return", "Go back", None, None)

        hostContextBinder.bind("key", params) shouldBe Some(Right(expected))
      }

      "fail to bind if a required parameter is missing" in {
        val params = Map(
          "returnLinkText" -> Seq("Go back"),
          "email"          -> Seq("test@example.com")
        )
        val expectedError = "Missing required query parameter(s): returnUrl"

        hostContextBinder.bind("key", params) shouldBe Some(Left(expectedError))
      }
    }

    "unbinding" should {

      "unbind a HostContext with only required fields" in {
        val hostContext = HostContext("https://www.example.com", "Back", None, None)
        val result = hostContextBinder.unbind("key", hostContext)

        result should include("returnUrl=Z88PydbLo24LElXdxb5xplQ9%252FJOglxwHnrkt6GCthEc%253D")
        result should include("returnLinkText=eglPb%252BfZkjBtGO3QF2unjg%253D%253D")
        result should not include "email="
        result should not include "cohort="
      }

      "unbind a HostContext with all fields populated" in {
        val hostContext = HostContext("https://www.example.com", "Back", Some(42), Some("user@host.com"))
        val result = hostContextBinder.unbind("key", hostContext)

        result should include("returnUrl=Z88PydbLo24LElXdxb5xplQ9%252FJOglxwHnrkt6GCthEc%253D")
        result should include("returnLinkText=eglPb%252BfZkjBtGO3QF2unjg%253D%253D")
        result should include("email=Nz5xSJGmYMuG%252BpwnPQKakw%253D%253D")
        result should include("cohort=OwtRN9DZR7TZVu59Lz%252BhZw%253D%253D")
      }

      "unbind a HostContext with some optional fields" in {
        val hostContext = HostContext("https://www.example.com", "Back", None, Some("user@host.com"))
        val result = hostContextBinder.unbind("key", hostContext)

        result should include("returnUrl=Z88PydbLo24LElXdxb5xplQ9%252FJOglxwHnrkt6GCthEc%253D")
        result should include("returnLinkText=eglPb%252BfZkjBtGO3QF2unjg%253D%253D")
        result should include("email=Nz5xSJGmYMuG%252BpwnPQKakw%253D%253D")
        result should not include "cohort="
      }
    }
  }
}
