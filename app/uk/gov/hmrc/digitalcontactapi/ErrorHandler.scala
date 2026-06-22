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
