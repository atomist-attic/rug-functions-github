package com.atomist.rug.function.github

import java.time.{OffsetDateTime, ZoneId}
import java.util.Date

import com.atomist.rug.runtime.Rug
import com.atomist.source.git.github.GitHubServices

trait GitHubFunction extends Rug {

  /**
    * Sanitize a token.
    */
  def safeToken(token: String): String =
    if (token != null)
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    else
      null

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

  def convertDate(date: Date): OffsetDateTime =
    if (date == null) null else OffsetDateTime.ofInstant(date.toInstant, ZoneId.systemDefault())
}
