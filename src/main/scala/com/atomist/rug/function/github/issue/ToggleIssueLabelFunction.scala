package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Add label or remove label from an issue.
  */
class ToggleIssueLabelFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "label-github-issue", description = "Adds a label to an existing issue or removes the label if already there",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "label") label: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking labelIssue with number '$number', label '$label', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      val issue = ghs.getIssue(repo, owner, number).get
      val existingLabels = issue.labels.map(_.name)
      val labels =
        if (existingLabels.exists(_ == label))
          existingLabels.filterNot(_ == label).toSeq
        else
          (existingLabels :+ label).toSeq

      val msg = if (labels.size > existingLabels.size) "added label to" else "removed label from"
      val response = ghs.editIssue(repo, owner, issue.number, issue.title, issue.body, issue.state, labels, issue.assignee.map(_.login).toList)
      FunctionResponse(Status.Success, Some(s"Successfully $msg issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to add label or remove label from issue `#$number` in `$owner/$repo`"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}

