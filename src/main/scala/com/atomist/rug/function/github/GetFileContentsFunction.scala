package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi._
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.source.git.GitHubServices
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.binary.Base64

import scala.util.{Failure, Success, Try}

/**
  * Display the content of a file
  */
class GetFileContentsFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "get-file-contents",
    description = "Fetch the content of one file on the default branch",
    tags = Array(new Tag(name = "github")))
  def invoke(@Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Parameter(name = "path") path: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Get File Contents invoked with owner '$owner', repo '$repo', apiUrl '$apiUrl' and token '${safeToken(token)}'")

    val ghs = GitHubServices(token, apiUrl)
    Try {
      ghs.getFileContents(repo, owner, path)
    } match {
      case Success(contents) =>
        if (contents.isEmpty) {
          FunctionResponse(Status.Success, Option(s"File not found"), None, None)
        }
        else {
          val decoded = new String(Base64.decodeBase64(contents.head.content))
          FunctionResponse(Status.Success, Option(s"File is there"), None, JsonBodyOption(decoded))
        }
      case Failure(e) =>
        val msg = s"Failed to fetch path ${path} in '$apiUrl' for `$owner/$repo`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
