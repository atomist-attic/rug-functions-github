package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.GitHubServices
import com.typesafe.scalalogging.LazyLogging

class SearchIssuesFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "search-github-issues", description = "Search for GitHub issues",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "search") search: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking searchIssues with search '$search', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)

      val params = Map("per_page" -> "100",
        "q" -> s"repo:$owner/$repo state:open",
        "sort" -> "updated",
        "order" -> "asc")

      val issues = ghs.searchIssues(params)
        .filter(i => (search == null || search == "not-set") || ((i.body != null && i.body.contains(search)) || (i.title != null && i.title.contains(search))))
        .sortWith((i1, i2) => i1.updatedAt.compareTo(i2.updatedAt) > 0)
        .map(i => {
          val id = i.number
          val title = i.title
          val urlStr = i.url
          // https://api.github.com/repos/octocat/Hello-World/issues/1347
          val url = urlStr.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/$id", "")
          // https://github.com/atomisthq/bot-service/issues/72
          val issueUrl = urlStr.replace("https://api.github.com/repos/", "https://github.com/")
          // atomisthq/bot-service
          val repo = urlStr.replace("https://api.github.com/repos/", "").replace(s"/issues/$id", "")
          val ts = i.updatedAt.toInstant.getEpochSecond
          GitHubIssue(id, title, url, issueUrl, repo, ts, i.state, i.assignee.orNull)
        }).slice(0, 10)
      FunctionResponse(Status.Success, Some(s"Successfully listed issues for search `$search` on `$repo/$owner`"), None, JsonBodyOption(issues))
    } catch {
      // Need to catch Throwable as Exception lets through GitHub message errors
      case t: Throwable =>
        val msg = "Failed to search issues"
        logger.warn(msg, t)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(t.getMessage))
    }
  }
}
