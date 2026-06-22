/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import play.api.Configuration
import play.api.mvc.QueryStringBindable

trait ExternalConfig {

  val configuration: Configuration

  lazy val host: String = configuration.get[String]("platform-frontend.host")

  private def queryParams(hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    play.core.routing.queryString(List(Some(b.unbind("hostContext", hostContext))))

  private def build(path: String, hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    s"$host$path${queryParams(hostContext)}"

  def checkSettingsUrl(hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    build("/paperless/check-settings", hostContext)

  def optInUrl(hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    build("/paperless/choose", hostContext)

  def reOptInUrl(cohort: Int, hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    build(s"/paperless/choose/$cohort", hostContext)

  def bounceUrl(hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    build("/paperless/email-bounce", hostContext)

  def emailReVerifyUrl(hostContext: HostContext)(implicit b: QueryStringBindable[HostContext]): String =
    build("/paperless/email-re-verify", hostContext)
}
