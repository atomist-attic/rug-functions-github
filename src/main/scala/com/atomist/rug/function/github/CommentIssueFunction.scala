package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction.convertDate
import com.atomist.rug.function.github.GitHubIssues._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.fasterxml.jackson.annotation.JsonProperty
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHIssueComment, GitHub}

import scala.util.{Failure, Success, Try}

/**
  * Adds a comment to an issue.
  */
class CommentIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  import CommentIssueFunction._

  @RugFunction(name = "comment-github-issue", description = "Adds a new comment to an issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "comment") comment: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String): FunctionResponse = {

    logger.info(s"Invoking commentIssue with number '$number', comment '$comment', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      mapIssueComment(issue.comment(comment))
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully added comment to issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to add comment to issue `#$number` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }

  private def mapIssueComment(ghIssueComment: GHIssueComment): Comment = {
    val gHUser = ghIssueComment.getUser
    val user = ResponseUser(gHUser.getLogin, gHUser.getId, gHUser.getUrl.toExternalForm, gHUser.getHtmlUrl.toExternalForm)

    Comment(ghIssueComment.getId, ghIssueComment.getUrl.toExternalForm, ghIssueComment.getBody, user,
      convertDate(ghIssueComment.getCreatedAt), convertDate(ghIssueComment.getUpdatedAt))
  }
}

object CommentIssueFunction {

  case class Comment(id: Int,
                     @JsonProperty("html_url") url: String,
                     body: String,
                     user: ResponseUser,
                     @JsonProperty("created_at") createdAt: OffsetDateTime,
                     @JsonProperty("updated_at") updatedAt: OffsetDateTime)
}
