package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction._
import com.atomist.rug.function.github.GitHubIssues.{GitHubIssue, Issue}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.reflect.Manifest
import scala.util.{Failure, Success, Try}
import scalaj.http.Http

class ListUserIssuesFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "list-github-user-issues",
    description = "List issues for user that owns the token",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "days") days: String = "1",
             @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String): FunctionResponse = {

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

      val time: OffsetDateTime = Try(days.toInt) match {
        case Success(d) => OffsetDateTime.now.minusDays(d)
        case Failure(_) => OffsetDateTime.now.minusDays(1)
      }

      issues.filter(i => i.updatedAt.isAfter(time) || (i.repository.isDefined && i.repository.get.pushedAt.isAfter(time)))
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

  private def getIssues(token: String, params: Map[String, String]): Seq[Issue] = {
    val response = Http(s"$ApiUrl/issues").params(params)
      .headers(getHeaders(token))
      .execute(is => fromJson[Seq[Issue]](is))
      .throwError

    val body = response.body
    val linkHeader = parseLinkHeader(response.header("Link"))
    linkHeader.get("next") match {
      case Some(url) => paginateResults(token, body, url, params)
      case None => body
    }
  }

  /**
    * Paginates a search and returns an aggregated list of results.
    */
  private def paginateResults[T](token: String,
                                 firstPage: Seq[T],
                                 url: String,
                                 params: Map[String, String] = Map.empty)
                                (implicit m: Manifest[T]): Seq[T] = {
    def nextPage(token: String, url: String, accumulator: Seq[T]): Seq[T] = {
      val response = Http(url).params(params)
        .headers(getHeaders(token))
        .execute(is => fromJson[Seq[T]](is))
        .throwError

      val pages = accumulator ++ response.body
      val linkHeader = parseLinkHeader(response.header("Link"))
      linkHeader.get("next") match {
        case Some(nextUrl) => nextPage(token, nextUrl, pages)
        case None => pages
      }
    }

    nextPage(token, url, firstPage)
  }
}