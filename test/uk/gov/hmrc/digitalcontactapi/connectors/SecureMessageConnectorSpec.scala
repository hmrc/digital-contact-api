/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.connectors

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class SecureMessageConnectorSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  "SecureMessageConnector" should {
    "forward the request to SecureMessage service" in {
      val servicesConfig = new ServicesConfig(
        Configuration(
          "microservice.services.secure-message.host"     -> "host",
          "microservice.services.secure-message.port"     -> 443,
          "microservice.services.secure-message.protocol" -> "https"
        )
      )

      val mockHttpClient = mock[HttpClientV2]
      val requestBuilder = mock[RequestBuilder]

      when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any)).thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            HttpResponse(
              OK,
              """{
                |	"count": {
                |		"total": 3,
                |		"unread": 3
                |	}
                |}""".stripMargin
            )
          )
        )

      implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      val connector = new SecureMessageConnector(servicesConfig, mockHttpClient)

      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest().withHeaders(("Authorization", "Bearer abc"))

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = connector.forwardGetRequest("/secure-messaging/messages/count")
      contentAsString(result) should include(""""total": 3""")
      contentAsString(result) should include(""""unread": 3""")
      status(result) shouldBe OK

    }

    "throw RuntimeException when host config is missing" in {
      val mockHttp = mock[HttpClientV2]

      assertThrows[RuntimeException] {
        new SecureMessageConnector(new ServicesConfig(Configuration.empty), mockHttp)
      }
    }

    "forward request with query parameters" in {
      val fixture = new SetUp()
      import fixture.*

      val urlCaptor: ArgumentCaptor[URL] = ArgumentCaptor.forClass(classOf[URL])
      when(mockHttpClient.get(urlCaptor.capture())(any[HeaderCarrier])).thenReturn(requestBuilder)
      stubResponse(OK, "{}")

      implicit val request: FakeRequest[?] = FakeRequest("GET", "/messages?tag=abc&filter=new")

      connector.forwardGetRequest("/messages").map { _ =>
        val urlString = urlCaptor.getValue.toString
        urlString should (include("(tag,abc)") and include("(filter,new)"))
      }
    }

    "skip Host header if host is null" in {
      val mockConfig = mock[ServicesConfig]
      val mockHttp = mock[HttpClientV2]
      val requestBuilder = mock[RequestBuilder]

      when(mockConfig.baseUrl(any[String])).thenReturn("http://localhost:8080")
      when(mockConfig.getConfString("secure-message.host", null)).thenReturn(null)

      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK, "ok")))

      val connector = new SecureMessageConnector(mockConfig, mockHttp)
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      implicit val hc: HeaderCarrier = HeaderCarrier()

      connector.forwardGetRequest("/messages").map { result =>
        result.header.status shouldBe OK
      }
    }

    "return response headers from upstream" in {
      val fixture = new SetUp()
      import fixture.*

      stubResponse(OK, "ok", Map("X-Custom-Header" -> Seq("Value")))
      implicit val request: FakeRequest[?] = FakeRequest()

      connector.forwardGetRequest("/messages").map { result =>
        result.header.headers.get("X-Custom-Header") shouldBe Some("Value")
      }
    }
  }

  private def servicesConfig(host: String = "localhost", port: Int = 8080, protocol: String = "http"): ServicesConfig =
    new ServicesConfig(
      Configuration(
        "microservice.services.secure-message.host"     -> host,
        "microservice.services.secure-message.port"     -> port,
        "microservice.services.secure-message.protocol" -> protocol
      )
    )

  class SetUp {
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    implicit val hc: HeaderCarrier = HeaderCarrier()

    when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
    when(requestBuilder.setHeader(any)).thenReturn(requestBuilder)

    def stubResponse(status: Int, body: String, headers: Map[String, Seq[String]] = Map.empty): Unit =
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(status, body, headers)))

    def defaultServicesConfig: ServicesConfig = servicesConfig()

    def connector(implicit ec: ExecutionContext): SecureMessageConnector =
      new SecureMessageConnector(defaultServicesConfig, mockHttpClient)
  }
}
