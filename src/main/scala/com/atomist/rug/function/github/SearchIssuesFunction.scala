package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.ListIssues
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

class SearchIssuesFunction
  extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction{

  @RugFunction(name = "search-github-issues", description = "Search for Github issues",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "search") search: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking listIssues with search '$search', owner '$owner', repo '$repo' and token '${safeToken(token)}'");

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val li = new ListIssues
    li.setDirection("asc")
    li.setState("open")
    li.setSort("updated")

    val cri = SimpleCloudRepoId(owner, repo)

    try{

      var issues = gitHubServices.listIssues(cri, li).asScala
      val result: Seq[GitHubIssue] = issues.filter(i => i.pullRequest == null)
        .filter(i => (search == null || search == "not-set") || ((i.body != null && i.body.contains(search)) || (i.title != null && i.title.contains(search)))).sortWith((i1, i2) => i1.updatedAt.compareTo(i2.updatedAt) > 0).toList.map(i => {
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
      }).slice(0, 10)
      FunctionResponse(Status.Success, Some(s"Successfully listed issues for search `$search` on `$repo/$owner`"), None, JsonBodyOption(result))
    } catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Failed to list issues"), None, StringBodyOption(e.getMessage))
    }
  }
}
