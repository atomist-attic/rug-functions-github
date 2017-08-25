package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

class SearchIssuesFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "search-github-issues", description = "Search for GitHub issues",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "q") q: String,
             @Parameter(name = "page") page: Int = 1,
             @Parameter(name = "perPage") perPage: Int = 10,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking searchIssues with q '$q', owner '$owner', repo '$repo', page '$page' and prePage '$perPage' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)

      val params = Map("per_page" -> perPage.toString,
        "page" -> page.toString,
        "q" -> s"repo:$owner/$repo $q",
        "sort" -> "updated",
        "order" -> "desc")

      val issues = ghs.searchIssues(params).items
        .map(i => {
          val id = i.number
          val title = i.title
          val urlStr = i.url
          // https://api.github.com/repos/octocat/Hello-World/issues/1347
          val url = urlStr.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/$id", "")
          // https://github.com/atomisthq/bot-service/issues/72
          val issueUrl = urlStr.replace("https://api.github.com/repos/", "https://github.com/")
          // atomisthq/bot-service
          val repository = urlStr.replace("https://api.github.com/repos/", "").replace(s"/issues/$id", "")
          val ts = i.updatedAt.toInstant.getEpochSecond
          val commits = ghs.listIssueEvents(repo, owner, i.number)
            .flatMap(_.commitId)
            .distinct
            .flatMap(ghs.getCommit(repo, owner, _))
            .map(c => IssueCommit(c.sha, c.url, c.commit.message))

          GitHubIssue(id, title, url, issueUrl, repository, ts, i.state, i.assignee.orNull, commits)
        })
      FunctionResponse(Status.Success, Some(s"Successfully listed issues for search `$q` on `$repo/$owner`"), None, JsonBodyOption(issues))
    } catch {
      // Need to catch Throwable as Exception lets through GitHub message errors
      case t: Throwable =>
        val msg = "Failed to search issues"
        logger.warn(msg, t)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(t.getMessage))
    }
  }
}
