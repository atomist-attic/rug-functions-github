package com.atomist.rug.function.github.reaction

import java.lang.{Iterable => JIterable}

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.reaction.CreateReactionFunction.{CommentReactableKey, ReactableKey}
import com.atomist.rug.function.github.reaction.GithubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{Reactable => GHReactable, _}

/**
  * Extend to create a GitHub reaction on an reactable
  */
trait CreateReactionFunction[T <: ReactableKey]
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  type Reactable = GHReactable with GHObject

  def retrieveReactable(repository: GHRepository, reactableKey: T): Reactable

  def createReaction(reaction: String,
                     reactableKey: T,
                     repo: String,
                     owner: String,
                     token: String,
                     apiUrl: String): FunctionResponse = {

    logger.info(s"Invoking createReaction on ${reactableKey.description} for owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = gitHubServices(token, apiUrl)
      ghs.getRepository(repo, owner)
        .map(repository => {
          val reactable = retrieveReactable(repository, reactableKey)
          val gHReaction = reactable.createReaction(ReactionContent.forContent(reaction))
          val response = Reaction(reactable.getId, reactable.getUrl, gHReaction.getContent.getContent)
          FunctionResponse(Status.Success, Some(s"Successfully added reaction '${response.content}' to '${reactableKey.description}"), None, JsonBodyOption(response))
        })
        .getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find repository `$repo` for owner `$owner`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to add reaction to '${reactableKey.description}"
        logger.warn(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }

  def retrieveComment[U <: GHObject](comments: JIterable[U], reactableKey: CommentReactableKey): U = {
    import scala.collection.JavaConverters._
    comments.asScala.find(_.getId == reactableKey.commentId) match {
      case Some(comment) => comment
      case None => throw new IllegalArgumentException(s"Cannot access '${reactableKey.description}.")
    }
  }

}

object CreateReactionFunction {

  trait ReactableKey {
    def description: String
  }

  trait CommentReactableKey extends ReactableKey {
    def commentId: Int
  }

}
