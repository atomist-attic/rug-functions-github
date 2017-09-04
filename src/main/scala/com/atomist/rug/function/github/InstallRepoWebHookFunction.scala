package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Install webhooks for repos.
  */
class InstallRepoWebHookFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  import GitHubFunction._

  @RugFunction(name = "install-github-repo-webhook", description = "Creates a new repo webhook",
    tags = Array(new Tag(name = "github"), new Tag(name = "webhooks")))
  def invoke(@Parameter(name = "url") url: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking installRepoWebhook with url '$url', owner '$owner', repo '$repo', apiUrl '$apiUrl' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      val wh = ghs.createWebhook(repo, owner, "web", url, "json", active = true, Events.toArray)
      val response = Map("id" -> wh.id, "name" -> wh.name, "url" -> wh.config.url, "content_type" -> wh.config.contentType, "events" -> wh.events)
      FunctionResponse(Status.Success, Some(s"Successfully installed repo-level webhook for `$owner/$repo`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create repo-level webhook for `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(ErrorMessage.jsonToString(e.getMessage)))
    }
  }
}
