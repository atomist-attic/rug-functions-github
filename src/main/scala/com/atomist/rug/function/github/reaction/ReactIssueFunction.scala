package com.atomist.rug.function.github.reaction

import java.net.URL

import com.atomist.rug.function.github.{ErrorMessage, GitHubFunction}
import com.atomist.rug.function.github.reaction.GitHubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.atomist.source.git.domain.ReactionContent
import com.typesafe.scalalogging.LazyLogging

/**
  * Reacts to a  GitHub issue
  */
class ReactIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "react-github-issue", description = "Reacts to a GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "issue") issueId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
    try {
      val ghs = GitHubServices(token, apiUrl)
      val react = ghs.createIssueReaction(repo, owner, issueId, ReactionContent.withName(reaction))
      val response = Reaction(react.id,  new URL(react.user.url), react.content.toString)
      FunctionResponse(Status.Success, Some(s"Successfully created issue reaction for `$issueId`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to add reaction to issue reaction`$issueId`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(ErrorMessage.jsonToString(e.getMessage)))
    }
  }
}