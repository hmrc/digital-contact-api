/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.controllers.internal

import controllers.Assets
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{ Action, AnyContent, ControllerComponents, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class DocumentationControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "DocumentationController.definition" should {
    "return OK when serving definition.json" in new SetUp {
      when(mockAssets.at("/public/api", "definition.json"))
        .thenReturn(stubAction)

      val result = controller.definition().apply(request)

      status(result) mustBe OK
    }
  }

  "DocumentationController.specification" should {
    "return OK when serving specification file" in new SetUp {
      when(mockAssets.at("/public/api/conf/1.0", "application.yaml"))
        .thenReturn(stubAction)

      val result = controller.specification("1.0", "application.yaml").apply(request)

      status(result) mustBe OK
    }
  }

  trait SetUp {
    val mockAssets: Assets = mock[Assets]
    val cc: ControllerComponents = stubControllerComponents()
    val controller = new DocumentationController(mockAssets, cc)

    val stubAction: Action[AnyContent] = cc.actionBuilder(Results.Ok)
    val request = FakeRequest("GET", "/api/definition")
  }
}
