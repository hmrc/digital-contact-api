/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.digitalcontactapi.model

import play.api.Logging
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.digitalcontactapi.model.QueryParamsCrypto.*

import scala.util.Try

trait QueryBindableSupport[A] extends Logging {

  def requiredParams: Seq[String]

  def optionalParams: Seq[String]

  def build(required: Map[String, String], optional: Map[String, Option[String]]): A

  protected def toFieldMap(value: A): Map[String, Option[String]]

  protected def mkBinder(using stringBinder: QueryStringBindable[String]): QueryStringBindable[A] =
    new QueryStringBindable[A] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, A]] = {

        def getValue(k: String): Option[String] =
          params.get(k).flatMap(_.headOption).flatMap(decodeQueryParams)

        val requiredValues: Map[String, Option[String]] =
          requiredParams.map(k => k -> getValue(k)).toMap

        val optionalValues: Map[String, Option[String]] =
          optionalParams.map(k => k -> getValue(k)).toMap

        val missing = requiredValues.collect { case (k, None) => k }.toList
        if (missing.nonEmpty) {
          val msg = s"Missing required query parameter(s): ${missing.mkString(", ")}"
          logger.error(msg)
          Some(Left(msg))
        } else {
          val requiredMap: Map[String, String] =
            requiredValues.collect { case (k, Some(v)) => k -> v }
          Some(Right(build(requiredMap, optionalValues)))
        }
      }

      override def unbind(key: String, value: A): String = {
        val fieldMap = toFieldMap(value)
        val parts = fieldMap.flatMap {
          case (k, Some(v)) =>
            encryptQueryParams(v).map(enc => summon[QueryStringBindable[String]].unbind(k, enc))
          case _ => None
        }
        parts.mkString("&")
      }
    }
}

final case class QueryParams(returnUrl: String, returnLinkText: String)

object QueryParams extends QueryBindableSupport[QueryParams] {
  override val requiredParams: Seq[String] = Seq("returnUrl", "returnLinkText")
  override val optionalParams: Seq[String] = Seq.empty[String]

  override def build(required: Map[String, String], optional: Map[String, Option[String]]): QueryParams =
    QueryParams(
      returnUrl = required("returnUrl"),
      returnLinkText = required("returnLinkText")
    )

  override protected def toFieldMap(value: QueryParams): Map[String, Option[String]] = Map(
    "returnUrl"      -> Some(value.returnUrl),
    "returnLinkText" -> Some(value.returnLinkText)
  )

  given queryStringBindable(using QueryStringBindable[String]): QueryStringBindable[QueryParams] = mkBinder
}

final case class HostContext(
  returnUrl: String,
  returnLinkText: String,
  cohort: Option[Int] = None,
  email: Option[String] = None
)

object HostContext extends QueryBindableSupport[HostContext] {
  override val requiredParams: Seq[String] = Seq("returnUrl", "returnLinkText")
  override val optionalParams: Seq[String] = Seq("email", "cohort")

  override def build(required: Map[String, String], optional: Map[String, Option[String]]): HostContext =
    HostContext(
      returnUrl = required("returnUrl"),
      returnLinkText = required("returnLinkText"),
      email = optional("email"),
      cohort = optional("cohort").flatMap(s => Try(s.toInt).toOption)
    )

  override protected def toFieldMap(value: HostContext): Map[String, Option[String]] = Map(
    "returnUrl"      -> Some(value.returnUrl),
    "returnLinkText" -> Some(value.returnLinkText),
    "email"          -> value.email,
    "cohort"         -> value.cohort.map(_.toString)
  )

  given queryStringBindable(using QueryStringBindable[String]): QueryStringBindable[HostContext] = mkBinder
}
