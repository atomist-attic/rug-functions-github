package com.atomist.rug.function.github

import com.atomist.rug.runtime.js.JsonSerializer
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.IssueComment
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

class CommentIssueFunction
  extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction{

  @RugFunction(name = "comment-issue", description = "Adds a new comment to an issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "number") number: Int,
             @Parameter(name = "comment") comment: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github/user_token=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking labelIssue with number '$number', comment '$comment', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)
    val issueComment = IssueComment(number, comment)


    try {
      val newComment = gitHubServices.createIssueComment(repoId, issueComment)

      FunctionResponse(Status.Success, Option(s"Successfully created new comment on issue `#${issueComment.number}` in `$owner/$repo`"), None, JsonBodyOption(newComment))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Failed to create new comment on issue `#${issueComment.number}` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
