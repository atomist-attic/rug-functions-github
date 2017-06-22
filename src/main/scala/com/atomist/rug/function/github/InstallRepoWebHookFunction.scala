package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubWebHooks.mapHook
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.github.GitHubServices
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GHEvent

import scala.collection.JavaConverters._

/**
  * Install webhooks for repos.
  */
class InstallRepoWebHookFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "install-github-repo-webhook", description = "Creates a new repo webhook",
    tags = Array(new Tag(name = "github"), new Tag(name = "webhooks")))
  def invoke(@Parameter(name = "url") url: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl", required = false) apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking installRepoWebhook with url '$url', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = apiUrl match {
        case url: String => GitHubServices(token, url)
        case _ => GitHubServices(token)
      }
      ghs.getRepository(repo, owner)
        .map(repository => {
          val config = Map("url" -> url, "content_type" -> "json")
          val gHHook = repository.createHook("web", config.asJava, Seq(GHEvent.ALL).asJava, true)
          val response = mapHook(gHHook)
          FunctionResponse(Status.Success, Some(s"Successfully installed repo-level webhook for `$owner/$repo`"), None, JsonBodyOption(response))
        })
        .getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find repository `$repo` for owner `$owner`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create repo-level webhook for `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
