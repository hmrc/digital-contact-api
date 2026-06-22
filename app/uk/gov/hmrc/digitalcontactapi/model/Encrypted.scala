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

package uk.gov.hmrc.digitalcontactapi.model

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import uk.gov.hmrc.crypto.*

import java.net.{ URLDecoder, URLEncoder }

case class Encrypted[T](decryptedValue: T)

object QueryParamsCrypto {
  lazy val currentCrypto: Encrypter & Decrypter =
    SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "queryParams.encryption", ConfigFactory.load())

  def encryptQueryParams(queryParams: String): Option[String] =
    try Some(encryptAndEncodeString(queryParams))
    catch {
      case e: Throwable =>
        LoggerFactory.getLogger("QueryParamsCrypto").warn(s"Unable to encrypt $queryParams : ${e.getMessage}")
        None
    }

  private def encryptAndEncodeString(s: String): String =
    URLEncoder.encode(currentCrypto.encrypt(PlainText(s)).value, "UTF-8")

  private def decode(s: String): String = URLDecoder.decode(s, "UTF-8")

  def decodeQueryParams(queryParams: String): Option[String] =
    try Some(decode(queryParams))
    catch {
      case e: Throwable =>
        LoggerFactory
          .getLogger("QueryParamsCrypto")
          .warn(s"Unable to decode query parameters $queryParams : ${e.getMessage}")
        None
    }
}
