package com.atomist.rug.function.github

import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.source.github.domain.EditIssue
import com.typesafe.scalalogging.LazyLogging

/**
  * Close a github issue
  */
class CloseIssueFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction
  with GitHubIssueEditor {

  @RugFunction(name = "close-github-issue", description = "Reopens a closed GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking close issue with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'");
    val issue = new EditIssue(number)
    issue.setState("closed")
    editIssue(issue, owner, repo, token)
  }
}
