package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging

/**
  * Merge a pull request.
  */
class MergePullRequestFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "merge-github-pull-request", description = "Merges a pull request",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "pr")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking mergePullRequest with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = gitHubServices(token, apiUrl)
      ghs.getRepository(repo, owner)
        .map(repository => {
          val pullRequest = repository.getPullRequest(number)
          pullRequest.merge(null)
          FunctionResponse(Status.Success, Some(s"Successfully merged pull request `$number"), None)
        })
        .getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find repository `$repo` for owner `$owner`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to merge pull request `$number"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
