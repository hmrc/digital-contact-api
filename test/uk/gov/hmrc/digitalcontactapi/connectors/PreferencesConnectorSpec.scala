/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.connectors

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{ any => anyArg }
import org.mockito.ArgumentCaptor

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }

import scala.concurrent.{ ExecutionContext, Future }
import java.net.{ URI, URL }
import play.api.http.Status.*

class PreferencesConnectorSpec extends AnyWordSpec with Matchers with MockitoSugar {

  implicit private val hc: HeaderCarrier = HeaderCarrier()
  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "PreferencesConnector.getPreferences" should {
    "call GET on {baseUrl}/preferences and return the HttpResponse from execute" in {
      val servicesConfig = mock[ServicesConfig]
      val httpClient = mock[HttpClientV2]
      val requestBuilder = mock[RequestBuilder]

      when(servicesConfig.baseUrl("preferences")).thenReturn("http://localhost:8025")

      when(httpClient.get(anyArg[URL])(anyArg[HeaderCarrier])).thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](anyArg[HttpReads[HttpResponse]], anyArg[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK, """{"ok":true}""", Map.empty)))

      val connector = new PreferencesConnector(servicesConfig, httpClient)

      val respF = connector.getPreferences()

      val resp = scala.concurrent.Await.result(respF, scala.concurrent.duration.Duration("2s"))
      resp.status mustBe OK

      val urlCaptor = ArgumentCaptor.forClass(classOf[URL])
      verify(httpClient).get(urlCaptor.capture())(anyArg[HeaderCarrier])
      urlCaptor.getValue mustBe new URI("http://localhost:8025/preferences").toURL

      verify(requestBuilder, times(1))
        .execute[HttpResponse](anyArg[HttpReads[HttpResponse]], anyArg[ExecutionContext])
    }
  }
}
