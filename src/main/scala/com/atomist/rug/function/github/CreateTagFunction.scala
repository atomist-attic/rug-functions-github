package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi._
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.git.github.domain.{CreateTagRequest, Tagger}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

/**
  * Create new tag on a commit.
  */
class CreateTagFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "create-github-tag", description = "Creates a new tag on a commit",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tag: String,
             @Parameter(name = "sha") sha: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.warn(s"Invoking createTag with tag '$tag', message '$message', sha '$sha', owner '$owner', repo '$repo', apiUrl '$apiUrl' and token '${safeToken(token)}'")

    val apiUrlSlash = if (apiUrl.endsWith("/")) apiUrl else s"$apiUrl/"
    val ghs = gitHubServices(token, apiUrlSlash)
    Try {
      val ctr = CreateTagRequest(tag, message, sha, "commit", Tagger("Atomist Bot", "bot@atomist.com", OffsetDateTime.now()))
      ghs createTag(repo, owner, ctr)
    } match {
      case Success(newTag) =>
        Try (ghs createReference(repo, owner, s"refs/tags/${newTag.tag}", newTag.sha))
         match {
          case Success(response) => FunctionResponse(Status.Success, Option(s"Successfully created annotated tag `$tag` in `$owner/$repo`"), None, JsonBodyOption(response))
          case Failure(e) =>
            val msg = s"Failed to create tag ref `$tag` on `$sha` in '$apiUrlSlash' for `$owner/$repo`"
            logger.error(msg, e)
            FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
        }
      case Failure(e) =>
        val msg = s"Failed to create tag object `$tag` on `$sha` in '$apiUrlSlash' for `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
