package com.atomist.rug.function.github

import com.atomist.util.JsonUtils
import com.fasterxml.jackson.annotation.JsonProperty

import scala.util.{Failure, Success, Try}

case class ErrorMessage(message: String,
                        @JsonProperty("documentation_url") documentationUrl: Option[String],
                        errors: Option[Seq[Error]]) {

  override def toString =
    message.toLowerCase + (errors match {
      case Some(e) => s" `${e.headOption.map(_.field).getOrElse("")}`"
      case None => ""
    })
}

object ErrorMessage {

  def jsonToString(json: String): String =
    Try(JsonUtils.fromJson[ErrorMessage](json)) match {
      case Success(em) => em.toString
      case Failure(_) => json
    }
}

case class Error(resource: String, field: String, code: String)
