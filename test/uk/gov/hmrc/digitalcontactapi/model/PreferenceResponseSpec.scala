/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import StatusNameResponse.*

class PreferenceResponseSpec extends AnyWordSpec with Matchers {

  "EmailPreference" must {

    "write to Json" in {
      val email = EmailPreference("test@test.com")
      Json.toJson(email) mustBe Json.obj("email" -> "test@test.com")
    }

    "read from Json" in {
      val json = Json.obj("email" -> "test@test.com")
      json.as[EmailPreference] mustBe EmailPreference("test@test.com")
    }
  }

  "StatusNameResponse" must {
    "read all valid status strings" in {
      JsString("PAPER").as[StatusNameResponse] mustBe StatusNameResponse.Paper
      JsString("EMAIL_NOT_VERIFIED").as[StatusNameResponse] mustBe StatusNameResponse.EmailNotVerified
      JsString("BOUNCED_EMAIL").as[StatusNameResponse] mustBe StatusNameResponse.BouncedEmail
      JsString("ALRIGHT").as[StatusNameResponse] mustBe StatusNameResponse.Alright
      JsString("NEW_CUSTOMER").as[StatusNameResponse] mustBe StatusNameResponse.NewCustomer
      JsString("NO_EMAIL").as[StatusNameResponse] mustBe StatusNameResponse.NoEmail
      JsString("OLD_VERSION").as[StatusNameResponse] mustBe StatusNameResponse.OldVersion
      JsString("RE_OPT_IN_MODIFIED").as[StatusNameResponse] mustBe StatusNameResponse.ReOptInModified
    }

    "write all status values" in {
      Json.toJson(StatusNameResponse.Paper) mustBe JsString("PAPER")
      Json.toJson(StatusNameResponse.EmailNotVerified) mustBe JsString("EMAIL_NOT_VERIFIED")
      Json.toJson(StatusNameResponse.BouncedEmail) mustBe JsString("BOUNCED_EMAIL")
      Json.toJson(StatusNameResponse.Alright) mustBe JsString("ALRIGHT")
      Json.toJson(StatusNameResponse.NewCustomer) mustBe JsString("NEW_CUSTOMER")
      Json.toJson(StatusNameResponse.NoEmail) mustBe JsString("NO_EMAIL")
      Json.toJson(StatusNameResponse.OldVersion) mustBe JsString("OLD_VERSION")
      Json.toJson(StatusNameResponse.ReOptInModified) mustBe JsString("RE_OPT_IN_MODIFIED")
    }

    "return JsError for invalid status" in {
      JsString("INVALID").validate[StatusNameResponse] mustBe a[JsError]
    }
  }

  "StatusNameResponse.toStatusName" should {
    "map all responses to correct StatusName" in {
      toStatusName(Paper) mustBe StatusName.Paper
      toStatusName(EmailNotVerified) mustBe StatusName.EmailNotVerified
      toStatusName(BouncedEmail) mustBe StatusName.BouncedEmail
      toStatusName(Alright) mustBe StatusName.Alright
      toStatusName(NewCustomer) mustBe StatusName.NewCustomer
      toStatusName(NoEmail) mustBe StatusName.NoEmail
      toStatusName(OldVersion) mustBe StatusName.ReOptIn
      toStatusName(ReOptInModified) mustBe StatusName.ReOptInModified
    }
  }

  "PaperlessStatusResponse" should {
    "read from Json" in {
      val json = Json.obj("name" -> "ALRIGHT")
      json.as[PaperlessStatusResponse] mustBe PaperlessStatusResponse(StatusNameResponse.Alright)
    }

    "write to Json" in {
      val response = PaperlessStatusResponse(StatusNameResponse.Paper)
      Json.toJson(response) mustBe Json.obj("name" -> "PAPER")
    }
  }

  "PreferenceResponse" should {
    "read with email and status" in {
      val json = Json.obj(
        "email"  -> Json.obj("email" -> "test@example.com"),
        "status" -> Json.obj("name" -> "ALRIGHT")
      )

      val result = json.as[PreferenceResponse]

      result.email mustBe Some(EmailPreference("test@example.com"))
      result.status mustBe Some(PaperlessStatusResponse(StatusNameResponse.Alright))
    }

    "read with missing optional fields" in {
      val json = Json.obj()

      val result = json.as[PreferenceResponse]

      result.email mustBe None
      result.status mustBe None
    }
  }

  "ValidateEmail" should {
    "read and write" in {
      val validateEmail = ValidateEmail("abc123")

      Json.toJson(validateEmail) mustBe Json.obj("token" -> "abc123")
      Json.obj("token" -> "abc123").as[ValidateEmail] mustBe validateEmail
    }
  }
}
