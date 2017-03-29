package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.reaction.CreateReactionFunction.CommentReactableKey
import com.atomist.rug.function.github.reaction.ReactPullRequestReviewCommentFunction.PullRequestReviewCommentReactableKey
import com.atomist.rug.spi.FunctionResponse
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import org.kohsuke.github.GHRepository

/**
  * Reacts to a GitHub pull request review comment
  */
class ReactPullRequestReviewCommentFunction extends CreateReactionFunction[PullRequestReviewCommentReactableKey] {

  override def retrieveReactable(repository: GHRepository, reactableKey: PullRequestReviewCommentReactableKey): Reactable = {
    val comments = repository.getPullRequest(reactableKey.pullRequestId).listReviewComments
    retrieveComment(comments, reactableKey)
  }

  @RugFunction(name = "react-github-pull-request-review-comment", description = "Reacts to a GitHub pull request review comment",
    tags = Array(new Tag(name = "github"), new Tag(name = "pull requests"), new Tag(name = "comments"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "pullRequestId") pullRequestId: Int,
             @Parameter(name = "commentId") commentId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
      createReaction(reaction, PullRequestReviewCommentReactableKey(pullRequestId, commentId), repo, owner, token)
  }
}

object ReactPullRequestReviewCommentFunction {
  case class PullRequestReviewCommentReactableKey(pullRequestId: Int, commentId: Int) extends CommentReactableKey {
    override def description: String = s"comment #$commentId of pull request #$pullRequestId"
  }
}
