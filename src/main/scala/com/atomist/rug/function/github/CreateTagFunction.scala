package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.spi.AnnotatedRugFunction
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.{CreateTag, Tagger}
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging

/**
  * Create new tag on a commit
  */
class CreateTagFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction{
  @RugFunction(name = "create-tag", description = "Creates a new tag on a commit",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tag: String,
             @Parameter(name = "sha") sha: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github/user_token=repo") token: String): Response = {

    logger.info(s"Invoking createTag with tag '$tag', message '$message', sha '$sha', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)
    val repoId = SimpleCloudRepoId(owner, repo)

    val date = OffsetDateTime.now()
    val cto = CreateTag(tag, message, sha, "commit", Tagger("Atomist Bot", "bot@atomist.com", date))

    try {
      gitHubServices.createAnnotatedTag(repoId, cto)
      Response(Status.Success, Option(s"Successfully create new tag `$tag` in `$owner/$repo`"), None, None)
    }
    catch {
      case e: Exception => Response(Status.Failure, Some(s"Failed to create new tag `$tag` in `$owner/$repo`"), None, Some(e.getMessage))
    }
  }
}
