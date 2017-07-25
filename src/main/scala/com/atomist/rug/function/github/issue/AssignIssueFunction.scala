package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Assigns an issue to a user.
  */
class AssignIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "assign-github-issue", description = "Assigns a GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "assignee") assignee: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking assignIssue with number '$number', assignee '$assignee', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      val issue = ghs.getIssue(repo, owner, number).get
      val assignees = issue.assignees.map(_.login) :+ assignee
      val response = ghs.editIssue(repo, owner, issue.number, issue.title, issue.body, issue.state, issue.labels.map(_.name), assignees)
      FunctionResponse(Status.Success, Some(s"Successfully assigned issue `#$number` in `$owner/$repo` to `$assignee`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to assign issue `#$number` in `$owner/$repo` to `$assignee`"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
