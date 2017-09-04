package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.{ErrorMessage, GitHubFunction}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Delete a branch.
  */
class DeleteBranchFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "delete-github-branch", description = "Merges a pull request",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "pr")))
  def invoke(@Parameter(name = "branch") branch: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking deleteBranch with branch '$branch', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      ghs.deleteBranch(repo, owner, branch)
      FunctionResponse(Status.Success, Some(s"Successfully deleted branch `$branch"), None)
    } catch {
      case e: Exception =>
        val msg = s"Failed to delete branch `$name`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(ErrorMessage.jsonToString(e.getMessage)))
    }
  }
}
