package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

/**
  * Adds a comment to an issue.
  */
class CommentIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "comment-github-issue", description = "Adds a new comment to an issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "comment") comment: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking commentIssue with number '$number', comment '$comment', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      issue.comment(comment)
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully added  comment to issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to add comment to issue `#$number` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
