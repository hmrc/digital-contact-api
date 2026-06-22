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

package uk.gov.hmrc.digitalcontactapi.controllers

import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisationException, AuthorisedFunctions }
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.*
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.digitalcontactapi.connectors.SecureMessageConnector

@Singleton
class SecureMessageController @Inject() (
  override val authConnector: AuthConnector,
  cc: ControllerComponents,
  secureMessageConnector: SecureMessageConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthorisedFunctions with Logging {

  val messagesCount: Action[AnyContent] = forwardToSecureMessage("/secure-messaging/messages/count")

  private def forwardToSecureMessage(uri: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      secureMessageConnector.forwardGetRequest(uri)
    }.recover { case _: AuthorisationException =>
      logger.error("UnAuthorised request for messageCount")
      Status(UNAUTHORIZED)
    }
  }
}
