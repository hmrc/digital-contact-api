/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi

import play.api.*
import play.api.mvc.*
import play.api.http.*
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.http.JsonErrorHandler

import javax.inject.*
import scala.concurrent.*

@Singleton
class ErrorHandler @Inject() (jsonErrorHandler: JsonErrorHandler) extends HttpErrorHandler {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    statusCode match {
      case BAD_REQUEST =>
        jsonErrorHandler.onClientError(request, statusCode, message)
        Future.successful(Results.BadRequest(Json.obj("error" -> message)))
      case _ =>
        jsonErrorHandler.onClientError(request, statusCode, message)
    }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    jsonErrorHandler.onServerError(request, exception)

}
