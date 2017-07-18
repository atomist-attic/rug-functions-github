package com.atomist.rug.function.github

import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.{OffsetDateTime, ZoneId}
import java.util.Date

import com.atomist.rug.runtime.Rug
import com.atomist.source.git.GitHubServices
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

trait GitHubFunction
  extends Rug {

  /**
    * Sanitize a token.
    */
  def safeToken(token: String): String =
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    } else {
      null
    }

  def gitHubServices(token: String, apiUrl: String): GitHubServices =
    apiUrl match {
      case url: String => GitHubServices(token, url)
      case _ => GitHubServices(token)
    }
}

object GitHubFunction {

  val ApiUrl = "https://api.github.com"

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
    .registerModule(new JavaTimeModule())
    .registerModule(new Jdk8Module())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, SerializationFeature.INDENT_OUTPUT)
    .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .setSerializationInclusion(Include.NON_NULL)
    .setSerializationInclusion(Include.NON_ABSENT)
    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"))

  def getHeaders(oAuthToken: String): Map[String, String] =
    Map("Authorization" -> ("token " + oAuthToken), "Accept" -> "application/vnd.github.v3+json")

  def toJson(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)

  def fromJson[T](is: InputStream)(implicit m: Manifest[T]): T = mapper.readValue[T](is)

  def parseLinkHeader(linkHeader: Option[String]): Map[String, String] = linkHeader match {
    case Some(lh) =>
      lh.split(',').map { part =>
        val section = part.split(';')
        val url = section(0).replace("<", "").replace(">", "").trim
        val name = section(1).replace(" rel=\"", "").replace("\"", "").trim
        (name, url)
      }.toMap
    case None => Map()
  }

  def convertDate(date: Date): OffsetDateTime =
    if (date == null) null else OffsetDateTime.ofInstant(date.toInstant, ZoneId.systemDefault())
}
