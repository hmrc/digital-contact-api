/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

class PreferenceStatusReturnResponseSpec extends AnyWordSpec with Matchers {

  "StatusName" must {
    "deserialize all valid status strings" in {
      JsString("PAPER").as[StatusName] mustBe StatusName.Paper
      JsString("EMAIL_NOT_VERIFIED").as[StatusName] mustBe StatusName.EmailNotVerified
      JsString("BOUNCED_EMAIL").as[StatusName] mustBe StatusName.BouncedEmail
      JsString("ALRIGHT").as[StatusName] mustBe StatusName.Alright
      JsString("NEW_CUSTOMER").as[StatusName] mustBe StatusName.NewCustomer
      JsString("NO_EMAIL").as[StatusName] mustBe StatusName.NoEmail
      JsString("RE_OPT_IN").as[StatusName] mustBe StatusName.ReOptIn
      JsString("RE_OPT_IN_MODIFIED").as[StatusName] mustBe StatusName.ReOptInModified
    }

    "serialize all status values" in {
      Json.toJson(StatusName.Paper) mustBe JsString("PAPER")
      Json.toJson(StatusName.EmailNotVerified) mustBe JsString("EMAIL_NOT_VERIFIED")
      Json.toJson(StatusName.BouncedEmail) mustBe JsString("BOUNCED_EMAIL")
      Json.toJson(StatusName.Alright) mustBe JsString("ALRIGHT")
      Json.toJson(StatusName.NewCustomer) mustBe JsString("NEW_CUSTOMER")
      Json.toJson(StatusName.NoEmail) mustBe JsString("NO_EMAIL")
      Json.toJson(StatusName.ReOptIn) mustBe JsString("RE_OPT_IN")
      Json.toJson(StatusName.ReOptInModified) mustBe JsString("RE_OPT_IN_MODIFIED")
    }

    "return JsError for invalid status" in {
      val result = JsString("INVALID").validate[StatusName]
      result mustBe a[JsError]
    }

    "include allowed values in error message" in {
      val JsError(errors) = JsString("INVALID").validate[StatusName]: @unchecked
      val errorMessage = errors.flatMap(_._2).map(_.message).mkString
      errorMessage must include("PAPER")
      errorMessage must include("ALRIGHT")
    }
  }

  "StatusName.fromStatus" must {
    "return Some for valid status strings" in {
      StatusName.fromStatus("PAPER") mustBe Some(StatusName.Paper)
      StatusName.fromStatus("ALRIGHT") mustBe Some(StatusName.Alright)
    }

    "return None for invalid status strings" in {
      StatusName.fromStatus("INVALID") mustBe None
      StatusName.fromStatus("") mustBe None
    }
  }

  "PreferenceStatusReturnResponse" must {
    "write to JSON" in {
      val response = PreferenceStatusReturnResponse(
        digital = true,
        status = StatusName.Alright,
        redirectUrl = "http://example.com"
      )

      val json = Json.toJson(response)

      (json \ "digital").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ALRIGHT"
      (json \ "redirectUrl").as[String] mustBe "http://example.com"
    }

    "read from JSON" in {
      val json = Json.obj(
        "digital"     -> true,
        "status"      -> "ALRIGHT",
        "redirectUrl" -> "http://example.com"
      )

      val result = json.as[PreferenceStatusReturnResponse]

      result.digital mustBe true
      result.status mustBe StatusName.Alright
      result.redirectUrl mustBe "http://example.com"
    }
  }
}
