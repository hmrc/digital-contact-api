/*
 * Copyright 2025 HM Revenue & Customs
 *
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
