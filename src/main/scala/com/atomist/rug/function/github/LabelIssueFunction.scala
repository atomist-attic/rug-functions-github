package com.atomist.rug.function.github

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.Handlers.Response
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.atomist.source.github.domain.EditIssue
import com.typesafe.scalalogging.LazyLogging

/**
  * Label an issue (with a known label)
  */
class LabelIssueFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction
  with GitHubIssueEditor {

  @RugFunction(name = "label-github-issue", description = "Adds a label to an already existing issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "number") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "label") label: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking labelIssue with number '$number', label '$label', owner '$owner', repo '$repo' and token '${safeToken(token)}'");

    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)
    val issue = githubservices.getIssue(repoId, number)

    val ei = new EditIssue(number)
    val labels = issue.labels.map(i => i.name).toSeq :+ label
    ei.addLabels(labels)
    editIssue(ei, owner, repo, token)
  }
}
