package com.atomist.rug.function.github

import com.atomist.param.{Parameter, ParameterValues, Tag}
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.{RugFunction, Secret}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.CreateIssue
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging


class CreateIssueFunction extends RugFunction with LazyLogging {

  override def run(parameters: ParameterValues): Response = {
    logger.info(s"Invoking createIssue with title '${parameters.stringParamValue("title")}', body '${parameters.stringParamValue("body")}', owner '${parameters.stringParamValue("owner")}', repo '${parameters.stringParamValue("repo")}' and token '${safeToken(parameters.stringParamValue("token"))}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(parameters.stringParamValue("token"))

    val repoId = new SimpleCloudRepoId(parameters.stringParamValue("owner"), parameters.stringParamValue("repo"))
    val issue = new CreateIssue(parameters.stringParamValue("title"))
    issue.setBody(parameters.stringParamValue("body"))

    try {
      val newIssue = gitHubServices.createIssue(repoId, issue)
      Response(Status.Success, Option(s"Successfully created new issue `#${newIssue.number}` in `${parameters.stringParamValue("owner")}/${parameters.stringParamValue("repo")}`"))
    }
    catch {
      case e: Exception => Response(Status.Failure, Option(e.getMessage))
    }
  }

  override def secrets: Seq[Secret] = Seq(Secret("user_token", "github"))

  override def name: String = "create-issue"

  override def description: String = "Creates and issue"

  override def tags: Seq[Tag] = Seq(Tag("github", "github"))

  override def parameters: Seq[Parameter] = Seq(Parameter("title"), Parameter("body"), Parameter("owner"), Parameter("repo"))

  private def safeToken(token: String): String = {
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    }
    else {
      null
    }
  }
}
