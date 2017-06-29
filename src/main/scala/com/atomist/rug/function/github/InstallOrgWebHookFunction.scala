package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubWebHooks.mapHook
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GHEvent

import scala.collection.JavaConverters._

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
             @Parameter(name = "apiUrl", required = false) apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=admin:org_hook") token: String): FunctionResponse = {

    logger.info(s"Invoking installOrgWebhook with url '$url', owner '$owner' and token '${safeToken(token)}'")

    try {
      val ghs = apiUrl match {
        case url: String => GitHubServices(token, url)
        case _ => GitHubServices(token)
      }
      val org = ghs.gitHub.getOrganization(owner)
      val config = Map("url" -> url, "content_type" -> "json")
      val gHHook = org.createHook("web", config.asJava, Seq(GHEvent.ALL).asJava, true)
      val response = mapHook(gHHook)
      FunctionResponse(Status.Success, Some(s"Successfully installed org-level webhook for `$owner`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create org-level webhook for `$owner`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
