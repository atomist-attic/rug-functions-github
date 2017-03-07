package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.github.domain.{CreateRelease, TagInfo}
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.atomist.source.{ArtifactSourceAccessException, SimpleCloudRepoId}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions


class CreateReleaseFunction extends AnnotatedRugFunction
  with LazyLogging
  with GitHubFunction{
  @RugFunction(name = "create-github-release", description = "Creates a new release based on a tag",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "tag") tagName: String,
             @Parameter(name = "message") message: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking create-release with tag '$tagName', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)

    var tags: java.util.List[TagInfo] = null

    var tag: Option[String] = Some(tagName)


    if (tagName == null || (tagName != null && tagName.length == 0)) {
      try {
        tags = gitHubServices.listTags(repoId)
      } catch {
        case e: ArtifactSourceAccessException =>
          return FunctionResponse(Status.Failure, Some(s"Failed to create release from tag `$tag` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
        case ex: Exception => throw ex
      }
      if (tags != null && tags.isEmpty) {
        tag = Option.apply(JavaConversions.asScalaBuffer(tags).head.name)
      }
    }

    if (tag.isEmpty) {
      return FunctionResponse(Status.Failure, Some(s"No tag found in `$owner/$repo`"))
    }

    try {
      val release = gitHubServices.createRelease(repoId, new CreateRelease(tag.get, "master", null, null, false, false))
      FunctionResponse(Status.Success, Some(s"Successfully created release `${release.tagName}` in `$owner/$repo#${release.targetCommitish}`"), None, JsonBodyOption(release))
    } catch {
      case e: ArtifactSourceAccessException =>
        FunctionResponse(Status.Failure, Some(s"Failed to create release from new tag `$tag` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
