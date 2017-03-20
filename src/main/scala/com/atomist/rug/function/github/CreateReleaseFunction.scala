package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

class CreateReleaseFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  @RugFunction(name = "create-github-release", description = "Creates a new release based on a tag",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tagName: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String): FunctionResponse = {

    logger.info(s"Invoking createRelease with tag '$tagName', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      repository.createRelease(tagName).draft(false).prerelease(false).name(null).commitish("master").create()
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully created release `${response.getTagName}` in `$owner/$repo#${response.getTargetCommitish}`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to create release from tag `$tagName` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
