/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.crypto.{ Crypted, PlainText } // Assuming imports from hmrc-crypto

import java.net.URLDecoder

class QueryParamsCryptoSpec extends AnyWordSpec with Matchers {

  private val crypto = QueryParamsCrypto

  "QueryParamsCrypto" should {

    val params = "returnLinkUrl=value1&returnLinkText=some sensitive data with spaces"

    "encryptQueryParams" should {
      "return a URL-encoded encrypted string for valid input" in {
        val encryptedResult = crypto.encryptQueryParams(params)

        encryptedResult shouldBe defined
        val encryptedString = encryptedResult.get

        encryptedString should not equal params

        val rawEncryptedValue = crypto.currentCrypto.encrypt(PlainText(params)).value
        URLDecoder.decode(encryptedString, "UTF-8") shouldBe rawEncryptedValue
      }
    }

    "decodeQueryParams" should {
      "successfully URL-decode a valid string" in {
        val encodedString = "returnLinkUrl%3Dvalue%26returnLinkText%3Danother%2Bvalue"
        val expectedDecodedString = "returnLinkUrl=value&returnLinkText=another+value"

        val decodedResult = crypto.decodeQueryParams(encodedString)

        decodedResult shouldBe Some(expectedDecodedString)
      }

      "return None if the string contains malformed URL encoding" in {
        val malformedString = "returnLinkUrl=value%&returnLinkText=value"

        val decodedResult = crypto.decodeQueryParams(malformedString)
        decodedResult shouldBe None
      }
    }

    "a full encryption-decryption cycle" should {
      "return the original value" in {
        val encryptedAndEncodedOpt = crypto.encryptQueryParams(params)
        encryptedAndEncodedOpt shouldBe defined
        val encryptedAndEncoded = encryptedAndEncodedOpt.get

        val decodedOpt = crypto.decodeQueryParams(encryptedAndEncoded)
        decodedOpt shouldBe defined
        val decodedForDecryption = decodedOpt.get

        val decryptedPlainText: PlainText = crypto.currentCrypto.decrypt(Crypted(decodedForDecryption))

        decryptedPlainText.value shouldBe params
      }
    }
  }
}
