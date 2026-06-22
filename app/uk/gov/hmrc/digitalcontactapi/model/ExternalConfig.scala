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
