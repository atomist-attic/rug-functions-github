package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.reaction.CreateReactionFunction.CommentReactableKey
import com.atomist.rug.function.github.reaction.ReactCommitCommentFunction.CommitCommentReactableKey
import com.atomist.rug.spi.FunctionResponse
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import org.kohsuke.github.GHRepository

/**
  * Reacts to a GitHub commit comment
  */
class ReactCommitCommentFunction extends CreateReactionFunction[CommitCommentReactableKey] {

  override def retrieveReactable(repository: GHRepository, reactableKey: CommitCommentReactableKey): Reactable = {
    val comments = repository.getCommit(reactableKey.sha1).listComments()
    retrieveComment(comments, reactableKey)
  }

  @RugFunction(name = "react-github-commit-comment", description = "Reacts to a GitHub commit comment",
    tags = Array(new Tag(name = "github"), new Tag(name = "commits"), new Tag(name = "comments"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "sha1") sha1: String,
             @Parameter(name = "commentId") commentId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
    createReaction(reaction, CommitCommentReactableKey(sha1, commentId), repo, owner, token)
  }

}

object ReactCommitCommentFunction {
  case class CommitCommentReactableKey(sha1: String, commentId: Int) extends CommentReactableKey {
    override def description: String = s"comment #$commentId of commit #$sha1"
  }
}
