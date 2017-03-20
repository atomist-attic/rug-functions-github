package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.CreateIssue
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

/**
  * Open a github issue
  */
class CreateIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction{

  @RugFunction(name = "create-github-issue", description = "Creates an GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "title") title: String,
             @Parameter(name = "body") body: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking createIssue with title '$title', body '$body', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)
    val issue = CreateIssue(title)
    issue.setBody(body)

    try {
      val newIssue = gitHubServices.createIssue(repoId, issue)
      FunctionResponse(Status.Success, Some(s"Successfully created new issue `#${newIssue.number}` in `$owner/$repo`"), None, JsonBodyOption(newIssue))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Failed too create new issue in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
