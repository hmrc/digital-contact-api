/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{ Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, OFormat }

case class PreferenceStatusReturnResponse(digital: Boolean, status: StatusName, redirectUrl: String)

object PreferenceStatusReturnResponse:
  given OFormat[PreferenceStatusReturnResponse] = Json.format[PreferenceStatusReturnResponse]

enum StatusName(val status: String) {
  case Paper extends StatusName("PAPER")
  case EmailNotVerified extends StatusName("EMAIL_NOT_VERIFIED")
  case BouncedEmail extends StatusName("BOUNCED_EMAIL")
  case Alright extends StatusName("ALRIGHT")
  case NewCustomer extends StatusName("NEW_CUSTOMER")
  case NoEmail extends StatusName("NO_EMAIL")
  case ReOptIn extends StatusName("RE_OPT_IN")
  case ReOptInModified extends StatusName("RE_OPT_IN_MODIFIED")
}

object StatusName {

  private val statusNames: Map[String, StatusName] = StatusName.values.map(s => s.status -> s).toMap

  def fromStatus(s: String): Option[StatusName] = statusNames.get(s)

  implicit val format: Format[StatusName] = new Format[StatusName] {
    def reads(json: JsValue): JsResult[StatusName] =
      json.validate[String].flatMap { s =>
        statusNames.get(s) match {
          case Some(value) => JsSuccess(value)
          case None =>
            JsError(
              s"Invalid Status Name: '$s'. Allowed: ${statusNames.keys.toList.sorted.mkString(", ")}"
            )
        }
      }

    def writes(statusName: StatusName): JsValue =
      JsString(statusName.status)
  }
}
