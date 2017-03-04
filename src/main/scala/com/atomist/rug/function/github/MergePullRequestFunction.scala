package com.atomist.rug.function.github

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.PullRequestMerge
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

/**
  * Merge a pull request
  */
class MergePullRequestFunction
  extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction{

  @RugFunction(name = "merge-pull-request", description = "Merges a pull request",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "pr")))
  def invoke(@Parameter(name = "number") number: Int,
             @Parameter(name = "comment") comment: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking merge with number '$number', comment '$comment', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)
    val pr = gitHubServices.getPullRequest(repoId, number)


    try {
      gitHubServices.mergePullRequest(repoId, new PullRequestMerge(number, pr.head.sha))
      FunctionResponse(Status.Success, Option(s"Successfully merged pull request `${pr.number}"))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Failed to merge pull request `${pr.number}"), None, StringBodyOption(e.getMessage))
    }
  }
}
