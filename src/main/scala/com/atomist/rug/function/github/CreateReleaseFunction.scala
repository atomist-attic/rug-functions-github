package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.GitHubServices
import com.typesafe.scalalogging.LazyLogging

class CreateReleaseFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "create-github-release", description = "Creates a new release based on a tag",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tagName: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking createRelease with tag '$tagName', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, Option(apiUrl))
      val response = ghs.createRelease(repo, owner, tagName, "master", "", message)
      FunctionResponse(Status.Success, Some(s"Successfully created release `${response.tagName}` in `$owner/$repo#${response.targetCommitish}`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to create release from tag `$tagName` in `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}