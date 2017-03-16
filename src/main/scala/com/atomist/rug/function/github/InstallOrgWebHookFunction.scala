package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHEvent, GitHub}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * Install webhooks for orgs.
  */
class InstallOrgWebHookFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "install-github-org-webhook", description = "Creates a new org webhook",
    tags = Array(new Tag(name = "github"), new Tag(name = "webhooks")))
  def invoke(@Parameter(name = "url") url: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking installWebhook with url '$url', owner '$owner' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val org = gitHub.getOrganization(owner)
      val config = Map("url" -> url, "content_type" -> "json")
      org.createHook("web", config.asJava, Seq(GHEvent.ALL).asJava, true)
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully installed org-level webhook for `$owner`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to create org-level webhook for `$owner`"), None, StringBodyOption(e.getMessage))
    }
  }
}
