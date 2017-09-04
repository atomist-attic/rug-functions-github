package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.{ErrorMessage, GitHubFunction}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Create a review comment for a pull request.
  */
class CreateReviewCommentFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "create-review-comment", description = "Creates a review comment",
    tags = Array(new Tag(name = "github"), new Tag(name = "pull requests"), new Tag(name = "pr")))
  def invoke(@Parameter(name = "pullrequest") number: Int,
             @Parameter(name = "body") body: String,
             @Parameter(name = "sha") sha: String,
             @Parameter(name = "path") path: String,
             @Parameter(name = "position") position: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking createReviewComment for pull request '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      val response = ghs.createPullRequestReviewComment(repo, owner, number, body, sha, path, position)
      FunctionResponse(Status.Success, Some(s"Successfully created review comment for pull request `$number`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create review comment for pull request `$number"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(ErrorMessage.jsonToString(e.getMessage)))
    }
  }
}
