package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

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
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking mergePullRequest with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val pullRequest = repository.getPullRequest(number)
      pullRequest.merge(null)
    } match {
      case Success(_) => FunctionResponse(Status.Success, Some(s"Successfully merged pull request `$number"), None)
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to merge pull request `$number"), None, StringBodyOption(e.getMessage))
    }
  }
}
