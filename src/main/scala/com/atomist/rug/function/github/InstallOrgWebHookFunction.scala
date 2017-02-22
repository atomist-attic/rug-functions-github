package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHEvent, GitHub}

import scala.collection.JavaConverters._

/**
  * Install webhooks for orgs
  */
class InstallOrgWebHookFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "install-github-org-webhook", description = "Creates a new org webhook",
    tags = Array(new Tag(name = "github"), new Tag(name = "webhooks")))
  def invoke(@Parameter(name = "url") url: String,
                 @Parameter(name = "owner") owner: String,
                 @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking installWebhook with url '$url', owner '${owner}' and token '${safeToken(token)}'")

    val config = new java.util.HashMap[String, String]()
    config.put("url", url)
    config.put("content_type", "json")
    val events = Seq(GHEvent.ALL)

    try {
      val github = GitHub.connectUsingOAuth(token)
      val response = github.getOrganization(owner).createHook("web", config, events.asJava, true)
      FunctionResponse(Status.Success, Option(s"Successfully installed org-level webhook for `${owner}`"), None, JsonBodyOption(response))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Failed to create org-level webhook for `${owner}`"), None, StringBodyOption(e.getMessage))
    }
  }
}
