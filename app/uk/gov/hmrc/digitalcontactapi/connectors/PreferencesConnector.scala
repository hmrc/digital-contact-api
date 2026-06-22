/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.connectors

import play.api.Logging
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse, StringContextOps }
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.hmrc.http.HttpReads.Implicits._

@Singleton
class PreferencesConnector @Inject() (val servicesConfig: ServicesConfig, http: HttpClientV2)(implicit
  ec: ExecutionContext
) extends Logging {

  private val serviceUrl: String = servicesConfig.baseUrl("preferences")

  private val url = s"$serviceUrl/preferences"

  def getPreferences()(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    http.get(url"$url").execute[HttpResponse]

}
