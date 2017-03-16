package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.github.util.HttpMethods.Get
import com.atomist.source.github.util.RestGateway.httpRequest
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class ListUserIssues extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction
  with GitHubSearchIssues {

  @RugFunction(name = "list-github-user-issues",
    description = "List issues for user that owns the token",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "days") days: String = "1",
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking listUserIssues with days '$days' and token '${safeToken(token)}'")

    Try {
      val params = Map("per_page" -> "100",
        "state" -> "open",
        "sort" -> "updated",
        "direction" -> "desc",
        "filter" -> "assigned")

      var issues = new ListBuffer[Issue]
      issues ++= getIssues(token, params)

      val params2 = params + ("state" -> "closed")
      issues ++= getIssues(token, params2)

      val time: OffsetDateTime = days.toInt match {
        case e => OffsetDateTime.now.minusDays(e)
        case _ => OffsetDateTime.now.minusDays(1)
      }

      issues.filter(i => i.updatedAt.isAfter(time) || (i.pushedAt != null && i.pushedAt.isAfter(time)))
        .sortWith((i1, i2) => i2.updatedAt.compareTo(i1.updatedAt) > 0)
        .map(i => {
          val id = i.number
          val title = i.title
          // https://api.github.com/repos/octocat/Hello-World/issues/1347
          val url = i.url.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/${i.number}", "")
          // https://github.com/atomisthq/bot-service/issues/72
          val issueUrl = i.url.replace("https://api.github.com/repos/", "https://github.com/")
          // atomisthq/bot-service
          val repo = i.url.replace("https://api.github.com/repos/", "").replace(s"/issues/${i.number}", "")
          val ts = i.updatedAt.toEpochSecond
          GitHubIssue(id, title, url, issueUrl, repo, ts, i.state, i.assignee)
        })
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some("Successfully listed issues"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some("Failed to list issues"), None, StringBodyOption(e.getMessage))
    }
  }

  def getIssues(token: String, params: Map[String, AnyRef]): Seq[Issue] = {
    val path = "https://api.github.com/issues"
    val response = httpRequest[Seq[Issue]](token, path, Get, None, params)
    val obj = response.obj
    response.linkHeader.get("next").map(url => paginateResults(token, obj, url, params)).getOrElse(obj)
  }
}
