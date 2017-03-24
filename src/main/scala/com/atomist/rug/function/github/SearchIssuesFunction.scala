package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GHIssueSearchBuilder.Sort
import org.kohsuke.github.{GHDirection, GitHub}

import scala.collection.JavaConverters._

class SearchIssuesFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  import GitHubIssues._

  @RugFunction(name = "search-github-issues", description = "Search for GitHub issues",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "search") search: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking searchIssues with search '$search', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val response = gitHub.searchIssues().q(s"repo:$owner/$repo").order(GHDirection.ASC).sort(Sort.UPDATED).list().withPageSize(100)
      val issues = response.asScala.toSeq
        .filter(i => (search == null || search == "not-set") || ((i.getBody != null && i.getBody.contains(search)) || (i.getTitle != null && i.getTitle.contains(search))))
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
          val respUser =
            if (assignee == null)
              null
            else ResponseUser(assignee.getLogin, assignee.getId, assignee.getUrl.toExternalForm, assignee.getAvatarUrl, assignee.getHtmlUrl.toExternalForm)

          GitHubIssue(id, title, url, issueUrl, repo, ts, i.getState.name(), respUser)
        }).slice(0, 10)

      FunctionResponse(Status.Success, Some(s"Successfully listed issues for search `$search` on `$repo/$owner`"), None, JsonBodyOption(issues))
    } catch {
      // Need to catch Throwable as Exception lets through GitHub message errors
      case t: Throwable => FunctionResponse(Status.Failure, Some("Failed to list issues"), None, StringBodyOption(t.getMessage))
    }
  }
}
