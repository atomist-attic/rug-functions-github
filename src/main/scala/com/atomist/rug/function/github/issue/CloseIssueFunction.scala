package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.issue.GitHubIssues.mapIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

/**
  * Close a GitHub issue.
  */
class CloseIssueFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "close-github-issue", description = "Reopens a closed GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking closeIssue with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      issue.close()
      mapIssue(repository.getIssue(number))
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully closed issue `#$number` in `$owner/$repo`"), None, JsonBodyOption(response))
      case Failure(e) =>
        val msg = s"Failed to close issue `#$number` in `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
