package com.atomist.rug.function.github.issue

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.domain.Issue
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class ListUserIssuesFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "list-github-user-issues",
    description = "List issues for user that owns the token",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "days") days: String = "1",
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking listUserIssues with days '$days' and token '${safeToken(token)}'")

    Try {
      val ghs = gitHubServices(token, "")

      val params = Map("per_page" -> "100",
        "state" -> "open",
        "sort" -> "updated",
        "direction" -> "desc",
        "filter" -> "assigned")

      var issues = new ListBuffer[Issue]
      issues ++= ghs listIssues (params)

      val params2 = params + ("state" -> "closed")
      issues ++= ghs listIssues (params2)

      val time: OffsetDateTime = Try(days.toInt) match {
        case Success(d) => OffsetDateTime.now.minusDays(d)
        case Failure(_) => OffsetDateTime.now.minusDays(1)
      }

      issues.filter(i => i.updatedAt.isAfter(time))
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
          Map("number" -> id, "title" -> title, "url" -> url, "issueUrl" -> issueUrl,
            "repo" -> repo, "ts" -> ts, "state" -> i.state, "assignee" -> i.assignee.orNull)
        })
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some("Successfully listed issues"), None, JsonBodyOption(response))
      case Failure(e) =>
        val msg = "Failed to list issues"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}