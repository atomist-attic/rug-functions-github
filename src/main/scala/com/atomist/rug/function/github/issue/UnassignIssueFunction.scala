package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.issue.GitHubIssues.mapIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * Unassigns an issue from a user.
  */
class UnassignIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "unassign-github-issue", description = "Unassigns a GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "assignee") assignee: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking unassignIssue with number '$number', assignee '$assignee', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      val assignees = issue.getAssignees.asScala.filterNot(_.getLogin == assignee)
      issue.setAssignees(assignees.asJava)
      mapIssue(repository.getIssue(number))
    } match {
      case Success(response) => FunctionResponse(Status.Success, Option(s"Successfully unassigned issue `#$number` in `$owner/$repo` from `$assignee`"), None, JsonBodyOption(response))
      case Failure(e) =>
        val msg = s"Failed to unassign issue `#$number` in `$owner/$repo` from `$assignee`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
