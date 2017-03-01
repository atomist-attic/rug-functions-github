package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.spi.AnnotatedRugFunction
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.{Issue, ListIssues}
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class ListUserIssues extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction{

  @RugFunction(name = "list=user-issues", description = "List issues for user that owns the token",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "days") days: Int = 1,
             @Secret(name = "user_token", path = "github/user_token=repo") token: String): Response = {

    logger.info(s"Invoking listIssues with days '$days' and token '${safeToken(token)}'");

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val li = new ListIssues
    li.setDirection("desc")
    li.setSort("updated")
    li.setFilter("assigned")
    li.setState("open")

    val time: OffsetDateTime = days match {
      case e => OffsetDateTime.now.minusDays(e)
      case _ => OffsetDateTime.now.minusDays(1)
    }

    val cri = SimpleCloudRepoId(null, null)
    var issues = new ListBuffer[Issue]
    try {
      issues ++= gitHubServices.listIssuesForUser(cri, li).asScala
      li.setState("closed")
      issues ++= gitHubServices.listIssuesForUser(cri, li).asScala

      val result: Seq[GitHubIssue] = issues.filter(i => i.updatedAt.isAfter(time) || (i.pushedAt != null && i.pushedAt.isAfter(time)))
        .sortWith((i1, i2) => i2.updatedAt.compareTo(i1.updatedAt) > 0)
        .toList.map(i => {
        val id = i.number
        val title = i.title
        // https://api.github.com/repos/octocat/Hello-World/issues/1347
        val url = i.url.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/${i.number}", "")
        // https://github.com/atomisthq/bot-service/issues/72
        val issueUrl = i.url.replace("https://api.github.com/repos/", "https://github.com/")
        // atomisthq/bot-service
        val repo = i.url.replace("https://api.github.com/repos/", "").replace(s"/issues/${i.number}", "")
        val ts = i.updatedAt.toEpochSecond
        GitHubIssue(id, title, url, issueUrl, repo, ts, i.state)
      })
      Response(Status.Success, Some(s"Successfully listed issues"), None, Some(JavaConversions.seqAsJavaList(result)))
    }catch {
      case e: Exception => Response(Status.Failure, Some(s"Failed to list issues"), None, Some(e.getMessage))
    }
  }
}
