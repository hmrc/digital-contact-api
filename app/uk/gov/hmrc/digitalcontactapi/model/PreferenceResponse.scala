/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.digitalcontactapi.model

import play.api.libs.json.*

case class EmailPreference(email: String)

object EmailPreference {
  implicit val formats: OFormat[EmailPreference] = Json.format[EmailPreference]
}

case class PaperlessStatusResponse(
  name: StatusNameResponse
)

object PaperlessStatusResponse {
  implicit val formats: OFormat[PaperlessStatusResponse] = Json.format[PaperlessStatusResponse]
}

case class PreferenceResponse(
  email: Option[EmailPreference],
  status: Option[PaperlessStatusResponse] = None
)

object PreferenceResponse:
  given OFormat[PreferenceResponse] = Json.format[PreferenceResponse]

enum StatusNameResponse(val status: String) {
  case Paper extends StatusNameResponse("PAPER")
  case EmailNotVerified extends StatusNameResponse("EMAIL_NOT_VERIFIED")
  case BouncedEmail extends StatusNameResponse("BOUNCED_EMAIL")
  case Alright extends StatusNameResponse("ALRIGHT")
  case NewCustomer extends StatusNameResponse("NEW_CUSTOMER")
  case NoEmail extends StatusNameResponse("NO_EMAIL")
  case OldVersion extends StatusNameResponse("OLD_VERSION")
  case ReOptInModified extends StatusNameResponse("RE_OPT_IN_MODIFIED")
}

object StatusNameResponse {
  implicit val formats: Format[StatusNameResponse] = new Format[StatusNameResponse] {
    def reads(json: JsValue): JsResult[StatusNameResponse] = json match {
      case JsString("PAPER")              => JsSuccess(Paper)
      case JsString("EMAIL_NOT_VERIFIED") => JsSuccess(EmailNotVerified)
      case JsString("BOUNCED_EMAIL")      => JsSuccess(BouncedEmail)
      case JsString("ALRIGHT")            => JsSuccess(Alright)
      case JsString("NEW_CUSTOMER")       => JsSuccess(NewCustomer)
      case JsString("NO_EMAIL")           => JsSuccess(NoEmail)
      case JsString("OLD_VERSION")        => JsSuccess(OldVersion)
      case JsString("RE_OPT_IN_MODIFIED") => JsSuccess(ReOptInModified)
      case _                              => JsError("Invalid Status Name Response")
    }

    def writes(statusNameResponse: StatusNameResponse): JsValue = JsString(statusNameResponse.status)
  }

  def toStatusName(statusNameResponse: StatusNameResponse): StatusName =
    statusNameResponse match {
      case Paper            => StatusName.Paper
      case EmailNotVerified => StatusName.EmailNotVerified
      case BouncedEmail     => StatusName.BouncedEmail
      case Alright          => StatusName.Alright
      case NewCustomer      => StatusName.NewCustomer
      case NoEmail          => StatusName.NoEmail
      case OldVersion       => StatusName.ReOptIn
      case ReOptInModified  => StatusName.ReOptInModified
    }
}

object ValidateEmail {
  implicit val formats: OFormat[ValidateEmail] = Json.format[ValidateEmail]
}

case class ValidateEmail(token: String)
