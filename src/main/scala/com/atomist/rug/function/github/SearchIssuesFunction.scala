package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GHIssueSearchBuilder.Sort
import org.kohsuke.github.{GHDirection, GitHub}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class SearchIssuesFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction
    with GitHubSearchIssues {

  @RugFunction(name = "search-github-issues", description = "Search for Github issues",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "search") search: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking searchIssues with search '$search', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      gitHub.searchIssues().q(s"user:$owner repo:$repo").isOpen.order(GHDirection.ASC).sort(Sort.UPDATED).list()
    } match {
      case Success(response) =>
        val issues = response.asScala.iterator
          .filter(i => (search == null || search == "not-set") || ((i.getBody != null && i.getBody.contains(search)) || (i.getTitle != null && i.getTitle.contains(search))))
          .toList
          .sortWith((i1, i2) => i1.getUpdatedAt.compareTo(i2.getUpdatedAt) > 0)
          .map(i => {
            val id = i.getNumber
            val title = i.getTitle
            val urlStr = i.getUrl.toExternalForm
            // https://api.github.com/repos/octocat/Hello-World/issues/1347
            val url = urlStr.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/$id", "")
            // https://github.com/atomisthq/bot-service/issues/72
            val issueUrl = urlStr.replace("https://api.github.com/repos/", "https://github.com/")
            // atomisthq/bot-service
            val repo = urlStr.replace("https://api.github.com/repos/", "").replace(s"/issues/$id", "")
            val ts = i.getUpdatedAt.toInstant.getEpochSecond
            val assignee = i.getAssignee
            val respUser = ResponseUser(assignee.getLogin, assignee.getId, assignee.getUrl.toExternalForm, assignee.getHtmlUrl.toExternalForm)
            GitHubIssue(id, title, url, issueUrl, repo, ts, i.getState.name(), respUser)
          }).slice(0, 10)

        FunctionResponse(Status.Success, Some(s"Successfully listed issues for search `$search` on `$repo/$owner`"), None, JsonBodyOption(issues))
      case Failure(e) => FunctionResponse(Status.Failure, Some("Failed to list issues"), None, StringBodyOption(e.getMessage))
    }
  }
//
//  def getIssues(token: String, params: Map[String, AnyRef]): Seq[Issue] = {
//    val path = "https://api.github.com/issues"
//    val response = httpRequest[Seq[Issue]](token, path, Get, None, params)
//    val obj = response.obj
//    response.linkHeader.get("next").map(url => paginateResults(token, obj, url, params)).getOrElse(obj)
//  }
}
