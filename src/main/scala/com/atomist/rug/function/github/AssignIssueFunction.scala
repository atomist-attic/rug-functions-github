package com.atomist.rug.function.github

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.Assignees
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

class AssignIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction{

  @RugFunction(name = "assign-issue", description = "Assigns an GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "number") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "assignee") assignee: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking assignIssue with number '$number', assignee '$assignee', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)
    val issue = githubservices.getIssue(repoId, number)

    val assignees = Assignees(number, (issue.assignees.map(a => a.login).toSeq :+ assignee).toArray)

    try {
      val issue = githubservices.addAssignees(repoId, assignees)
      FunctionResponse(Status.Success, Option(s"Successfully assigned issue `#${issue.number}` in `$owner/$repo` to `$assignee`"), None, JsonBodyOption(issue))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Error assigning issue `#${issue.number}` in `$owner/$repo` to `$assignee`"), None, StringBodyOption(e.getMessage))
    }
  }
}
