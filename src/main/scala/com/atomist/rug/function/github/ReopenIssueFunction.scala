package com.atomist.rug.function.github

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.Handlers.Response
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.github.domain.EditIssue
import com.typesafe.scalalogging.LazyLogging

/**
  * Reopens a github issue
  */
class ReopenIssueFunction
  extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction
  with GitHubIssueEditor {

  @RugFunction(name = "reopen-github-issue", description = "Reopens a closed GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "number") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking reopenIssue with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'");
    val issue = new EditIssue(number)
    issue.setState("open")
    editIssue(issue, owner, repo, token)
  }
}
