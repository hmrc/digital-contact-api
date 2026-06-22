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

package uk.gov.hmrc.digitalcontactapi.controllers

import play.api.{ Configuration, Logging }
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.http.Status.OK
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisationException, AuthorisedFunctions }
import uk.gov.hmrc.digitalcontactapi.connectors.PreferencesConnector
import uk.gov.hmrc.digitalcontactapi.model.{ ExternalConfig, HostContext, PreferenceResponse, PreferenceStatusReturnResponse, QueryParams, StatusName, StatusNameResponse }
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.digitalcontactapi.model.StatusName.*
import uk.gov.hmrc.digitalcontactapi.model.HostContext.*

import javax.inject.*
import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class PreferencesController @Inject() (
  cc: ControllerComponents,
  preferencesConnector: PreferencesConnector,
  val authConnector: AuthConnector,
  override val configuration: Configuration
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with ExternalConfig with AuthorisedFunctions with Logging {

  private lazy val ReOptInPageCohort: Int = 55

  def preferences(implicit queryParams: QueryParams): Action[AnyContent] =
    Action.async { implicit request =>
      authorised() {
        preferencesConnector
          .getPreferences()
          .map { response =>
            response.status match {
              case OK =>
                val prefs = Json.parse(response.body).as[PreferenceResponse]
                Ok(Json.toJson(buildPreferenceStatus(Some(prefs))))

              case NOT_FOUND =>
                logger.warn(s"No preferences found. ${response.body}")
                Ok(Json.toJson(buildPreferenceStatus(None)))

              case anyOther =>
                val json = Try(Json.parse(response.body)).getOrElse(Json.obj("error" -> response.body))
                Status(anyOther)(json)
            }
          }
          .recover { case e: Throwable =>
            InternalServerError(Json.obj("error" -> e.getMessage))
          }
      }.recover { case _: AuthorisationException =>
        logger.error("UnAuthorised request for paperless preferences")
        Status(UNAUTHORIZED)
      }
    }

  private def buildPreferenceStatus(
    preference: Option[PreferenceResponse]
  )(implicit queryParams: QueryParams): PreferenceStatusReturnResponse = {

    val host: HostContext = HostContext(queryParams.returnUrl, queryParams.returnLinkText)

    val emailOpt: Option[String] =
      preference.collect { case PreferenceResponse(Some(emailPref), _) => emailPref.email }

    val statusName: StatusName =
      preference
        .flatMap(_.status)
        .map(s => StatusNameResponse.toStatusName(s.name))
        .getOrElse(NewCustomer)

    def buildResponse(isDigital: Boolean, url: String): PreferenceStatusReturnResponse =
      PreferenceStatusReturnResponse(isDigital, statusName, url)

    statusName match {
      case Alright => buildResponse(true, checkSettingsUrl(host))

      case NewCustomer => buildResponse(false, optInUrl(host))

      case NoEmail | Paper => buildResponse(false, checkSettingsUrl(host))

      case EmailNotVerified => buildResponse(false, emailReVerifyUrl(host.copy(email = emailOpt)))

      case BouncedEmail => buildResponse(false, bounceUrl(host.copy(email = emailOpt)))

      case ReOptIn | ReOptInModified =>
        val h = host.copy(email = emailOpt, cohort = Some(ReOptInPageCohort))
        PreferenceStatusReturnResponse(true, ReOptIn, reOptInUrl(ReOptInPageCohort, h))
    }
  }
}
