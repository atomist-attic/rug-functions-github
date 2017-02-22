package com.atomist.rug.function.github

import com.atomist.rug.spi.AnnotatedRugFunction
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.CreateIssue
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

class CreateIssueFunction extends AnnotatedRugFunction with LazyLogging {

  @RugFunction(name = "create-issue", description = "Creates an GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "title") title: String,
             @Parameter(name = "body") body: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github") token: String): Response = {

    logger.info(s"Invoking createIssue with title '${title}', body '${body}', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val issue = new CreateIssue(title)
    issue.setBody(body)

    try {
      val newIssue = gitHubServices.createIssue(repoId, issue)
      Response(Status.Success, Option(s"Successfully created new issue `#${newIssue.number}` in `${owner}/${repo}`"))
    }
    catch {
      case e: Exception => Response(Status.Failure, Option(e.getMessage))
    }
  }

  private def safeToken(token: String): String = {
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    }
    else {
      null
    }
  }
}
