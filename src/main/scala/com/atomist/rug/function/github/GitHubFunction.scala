package com.atomist.rug.function.github

import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.{OffsetDateTime, ZoneId}
import java.util.Date

import com.atomist.rug.runtime.Rug
import com.atomist.source.git.github.GitHubServices
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
    GitHubServices(token, Option(apiUrl))
}

object GitHubFunction {

  val ApiUrl = "https://api.github.com"

  val Events = Seq(
    "commit_comment",
    "create",
    "delete",
    "deployment",
    "deployment_status",
    "download",
    "follow",
    "fork",
    "fork_apply",
    "gist",
    "gollum",
    "issue_comment",
    "issues",
    "member",
    "page_build",
    "public",
    "pull_request",
    "pull_request_review_comment",
    "push",
    "release",
    "repository",
    "status",
    "team_add",
    "watch",
    "ping")

//  val mapper = new ObjectMapper() with ScalaObjectMapper
//  mapper.registerModule(DefaultScalaModule)
//    .registerModule(new JavaTimeModule())
//    .registerModule(new Jdk8Module())
//    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, SerializationFeature.INDENT_OUTPUT)
//    .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//    .setSerializationInclusion(Include.NON_NULL)
//    .setSerializationInclusion(Include.NON_ABSENT)
//    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"))
//
//  def getHeaders(oAuthToken: String): Map[String, String] =
//    Map("Authorization" -> ("token " + oAuthToken), "Accept" -> "application/vnd.github.v3+json")
//
//  def toJson(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)
//
//  def fromJson[T](is: InputStream)(implicit m: Manifest[T]): T = mapper.readValue[T](is)

  def convertDate(date: Date): OffsetDateTime =
    if (date == null) null else OffsetDateTime.ofInstant(date.toInstant, ZoneId.systemDefault())
}
