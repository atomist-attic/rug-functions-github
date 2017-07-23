package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.domain.{Webhook, WebhookInfo}
import com.typesafe.scalalogging.LazyLogging

/**
  * Install webhooks for orgs.
  */
class InstallOrgWebHookFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  import GitHubFunction._

  @RugFunction(name = "install-github-org-webhook", description = "Creates a new org webhook",
    tags = Array(new Tag(name = "github"), new Tag(name = "webhooks")))
  def invoke(@Parameter(name = "url") url: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=admin:org_hook") token: String): FunctionResponse = {

    logger.info(s"Invoking installOrgWebhook with url '$url', owner '$owner', apiUrl '$apiUrl' and token '${safeToken(token)}'")

    try {
      val ghs = gitHubServices(token, apiUrl)
      val hook = ghs.createOrganizationWebhook(owner, Webhook("web", url, "json", Events))
      val response = WebhookInfo(hook)
      FunctionResponse(Status.Success, Some(s"Successfully installed org-level webhook for `$owner`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create org-level webhook for `$owner`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
