package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.atomist.rug.function.github.GitHubFunction.convertDate
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

class CreateReleaseFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction {

  import CreateReleaseFunction._

  @RugFunction(name = "create-github-release", description = "Creates a new release based on a tag",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tagName: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking createRelease with tag '$tagName', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val gHRelease = repository.createRelease(tagName).draft(false).prerelease(false).name(null).commitish("master").create()
      Release(gHRelease.getId, gHRelease.getTagName, gHRelease.getTargetCommitish, gHRelease.getName, gHRelease.getBody,
        gHRelease.isDraft, gHRelease.isPrerelease, convertDate(gHRelease.getCreatedAt),
        convertDate(gHRelease.getPublished_at), gHRelease.getUploadUrl, gHRelease.getZipballUrl, gHRelease.getTarballUrl)
    } match {
      case Success(response) => FunctionResponse(Status.Success, Some(s"Successfully created release `${response.tagName}` in `$owner/$repo#${response.targetCommitish}`"), None, JsonBodyOption(response))
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to create release from tag `$tagName` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}

object CreateReleaseFunction {

  case class Release @JsonCreator()(@JsonProperty("id") id: Int,
                                    @JsonProperty("tag_name") tagName: String,
                                    @JsonProperty("target_commitish") targetCommitish: String,
                                    @JsonProperty("name") name: String,
                                    @JsonProperty("body") body: String,
                                    @JsonProperty("draft") draft: Boolean,
                                    @JsonProperty("prerelease") prerelease: Boolean,
                                    @JsonProperty("created_at") var createdAt: OffsetDateTime,
                                    @JsonProperty("published_at") var publishedAt: OffsetDateTime,
                                    @JsonProperty("upload_url") uploadlUrl: String,
                                    @JsonProperty("zipball_url") zipballUrl: String,
                                    @JsonProperty("tarball_url") tarballUrl: String)
}
