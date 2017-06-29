package com.atomist.rug.function.github.issue

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.GitHubFunction.convertDate
import com.atomist.rug.function.github.issue.GitHubIssues.ResponseUser
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.fasterxml.jackson.annotation.JsonProperty
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GHIssueComment

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
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking commentIssue with number '$number', comment '$comment', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token)
      ghs.getRepository(repo, owner)
        .map(repository => {
          val issue = repository.getIssue(number)
          val response = mapIssueComment(issue.comment(comment))
          FunctionResponse(Status.Success, Some(s"Successfully added comment to issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
        })
        .getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find repository `$repo` for owner `$owner`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to add comment to issue `#$number` in `$owner/$repo`"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }

  private def mapIssueComment(ghIssueComment: GHIssueComment): Comment = {
    val gHUser = ghIssueComment.getUser
    val user = ResponseUser(gHUser.getLogin, gHUser.getId, gHUser.getUrl.toExternalForm, gHUser.getAvatarUrl, gHUser.getHtmlUrl.toExternalForm)

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
