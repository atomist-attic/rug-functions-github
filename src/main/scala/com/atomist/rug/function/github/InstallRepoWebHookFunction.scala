package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHEvent, GitHub}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

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
             @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String): FunctionResponse = {

    logger.info(s"Invoking installRepoWebhook with url '$url', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val config = Map("url" -> url, "content_type" -> "json")
      repository.createHook("web", config.asJava, Seq(GHEvent.ALL).asJava, true)
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully installed repo-level webhook for `$owner/$repo`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to create repo-level webhook for `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
