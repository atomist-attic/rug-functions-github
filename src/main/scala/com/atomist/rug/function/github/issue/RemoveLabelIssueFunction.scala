package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.{ErrorMessage, GitHubFunction}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Removed a label from an issue.
  */
class RemoveLabelIssueFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "remove-label-github-issue", description = "Removes a label from an already existing issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "label") label: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking removeLabelIssue with number '$number', label '$label', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      ghs.getIssue(repo, owner, number) match {
        case Some(issue) =>
          val labels = issue.labels.map(_.name).filterNot(_ == label).toSeq
          val response = ghs.editIssue(repo, owner, issue.number, issue.title, issue.body, issue.state, labels, issue.assignee.map(_.login).toList)
          FunctionResponse(Status.Success, Some(s"Successfully removed label from issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
        case None =>
          val msg = s"Failed to find issue `#$number` in `$owner/$repo`"
          FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(msg))
      }
    } catch {
      case e: Exception =>
        val msg = s"Failed to remove label from issue `#$number` in `$owner/$repo`"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(ErrorMessage.jsonToString(e.getMessage)))
    }
  }
}

