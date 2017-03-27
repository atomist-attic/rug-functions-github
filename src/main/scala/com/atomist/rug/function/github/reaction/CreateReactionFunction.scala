package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.reaction.CreateReactionFunction.{CommentReactableKey, ReactableKey}
import com.atomist.rug.function.github.reaction.GithubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github._
import org.kohsuke.github.{Reactable => GHReactable}
import java.lang.{Iterable => JIterable}

import scala.util.{Failure, Success, Try}

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
             token: String): FunctionResponse = {
    logger.info(s"Invoking createReaction on ${reactableKey.description} for owner '$owner', repo '$repo' and token '${safeToken(token)}'")
    Try {val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val reactable = retrieveReactable(repository, reactableKey)
      val gHReaction = reactable.createReaction(ReactionContent.forContent(reaction))
      Reaction(reactable.getId, reactable.getUrl, gHReaction.getContent.getContent)
    }match {
      case Success(response) =>
        val msg = s"Successfully added reaction '${response.content}' to '${reactableKey.description}"
        FunctionResponse(Status.Success, Some(msg), None, JsonBodyOption(response))
      case Failure(e) =>
        var msg = s"Failed to add reaction to '${reactableKey.description}"
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
