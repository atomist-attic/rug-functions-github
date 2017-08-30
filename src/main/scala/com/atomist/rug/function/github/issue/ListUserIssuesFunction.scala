package com.atomist.rug.function.github.issue

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.atomist.source.git.domain.Issue
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

    try {
      val ghs = GitHubServices(token)

      val params = Map("per_page" -> "100",
        "state" -> "open",
        "sort" -> "updated",
        "direction" -> "desc",
        "filter" -> "assigned")

      var issueBuf = new ListBuffer[Issue]
      issueBuf ++= ghs.listIssues(params)

      val params2 = params + ("state" -> "closed")
      issueBuf ++= ghs.listIssues(params2)

      val time: OffsetDateTime = Try(days.toInt) match {
        case Success(d) => OffsetDateTime.now.minusDays(d)
        case Failure(_) => OffsetDateTime.now.minusDays(1)
      }

      val response = issueBuf.filter(_.updatedAt.isAfter(time))
        .sortWith((i1, i2) => i2.updatedAt.compareTo(i1.updatedAt) > 0)
        .map(i => {
          val id = i.number
          val title = i.title
          val ts = i.updatedAt.toEpochSecond
          val repoUrl = i.repository.map(_.htmlUrl).getOrElse("")
          val repository = i.repository.map(r => s"${r.ownerName}/${r.name}").getOrElse("")
          val commits = i.repository match {
            case Some(r) =>
              ghs.listIssueEvents(r.name, r.ownerName, i.number)
                .flatMap(_.commitId)
                .distinct
                .flatMap(ghs.getCommit(r.name, r.ownerName, _))
                .map(c => IssueCommit(c.sha, c.htmlUrl, c.commit.message))
            case None => Nil
          }

          GitHubIssue(id, title, repoUrl, i.htmlUrl, repository, ts, i.state, i.assignee.orNull, commits)
        }).distinct
      FunctionResponse(Status.Success, Some("Successfully listed issues"), None, JsonBodyOption(response))
    } catch {
      // Need to catch Throwable as Exception lets through GitHub message errors
      case t: Throwable =>
        val msg = "Failed to list issues"
        logger.warn(msg, t)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(t.getMessage))
    }
  }
}