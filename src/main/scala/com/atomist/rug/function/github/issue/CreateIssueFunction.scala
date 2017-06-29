package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.issue.GitHubIssues.mapIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Open a GitHub issue.
  */
class CreateIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "create-github-issue", description = "Creates a GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "title") title: String,
             @Parameter(name = "body") body: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking createIssue with title '$title', body '$body', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token)
      ghs.getRepository(repo, owner)
        .map(repository => {
          val gHIssue = repository.createIssue(title).body(body).create()
          val response = mapIssue(gHIssue)
          FunctionResponse(Status.Success, Some(s"Successfully created issue `#${response.number}` in `$owner/$repo`"), None, JsonBodyOption(response))
        })
        .getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find repository `$repo` for owner `$owner`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create issue in `$owner/$repo`"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
