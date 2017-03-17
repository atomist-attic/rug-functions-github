package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * Label an issue with a known label.
  */
class LabelIssueFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "label-github-issue", description = "Adds a label to an already existing issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "label") label: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking labelIssue with number '$number', label '$label', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      val labels = issue.getLabels.asScala.map(_.getName).toSeq :+ label
      issue.setLabels(labels: _*)
    } match {
      case Success(_) => FunctionResponse(Status.Success, Some(s"Successfully labelled issue `#$number` in `$owner/$repo`"), None)
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to label issue `#$number` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
