/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.connectors

import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.Status
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SecureMessageConnector @Inject() (
  val servicesConfig: ServicesConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {
  private val serviceName: String = "secure-message"

  private val baseUrl: String = servicesConfig.baseUrl(serviceName)

  private val host: String =
    servicesConfig.getConfString(
      s"$serviceName.host",
      throw new RuntimeException(s"Could not find config key $serviceName.host")
    )

  def forwardGetRequest(uri: String)(implicit request: RequestHeader, hc: HeaderCarrier): Future[Result] = {

    val base = s"$baseUrl$uri"

    val queryParams = request.queryString.toSeq.flatMap { case (k, vs) => vs.map(v => (k, v)) }
    val headers = {
      val baseHeaders = request.headers.toSimpleMap
      if (host != null) baseHeaders + ("Host" -> host) else baseHeaders
    }

    val fullUrl = if (queryParams.nonEmpty) s"$base?${queryParams.mkString("&")}" else base

    logger.debug(s"Forwarding GET request to $fullUrl")

    http
      .get(url"$fullUrl")
      .setHeader(headers.toSeq: _*)
      .execute[HttpResponse]
      .map(buildResult)
  }

  private def buildResult(response: HttpResponse): Result = {
    logger.warn(s"Response from $serviceName: ${response.status}")
    Status(response.status)(response.body)
      .withHeaders(response.headers.view.mapValues(_.head).toSeq: _*)
  }
}
