package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.reaction.CreateReactionFunction.CommentReactableKey
import com.atomist.rug.function.github.reaction.ReactIssueCommentFunction.IssueCommentReactableKey
import com.atomist.rug.spi.FunctionResponse
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import org.kohsuke.github.GHRepository

/**
  * Reacts to a GitHub issue comment
  */
class ReactIssueCommentFunction extends CreateReactionFunction[IssueCommentReactableKey] {

  override def retrieveReactable(repository: GHRepository, reactableKey: IssueCommentReactableKey): Reactable = {
    val comments = repository.getIssue(reactableKey.issueId).listComments()
    retrieveComment(comments, reactableKey)
  }

  @RugFunction(name = "react-github-issue-comment", description = "Reacts to a GitHub issue comment",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "comments"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "issueId") issueId: Int,
             @Parameter(name = "commentId") commentId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
      createReaction(reaction, IssueCommentReactableKey(issueId, commentId), repo, owner, token)
  }
}

object ReactIssueCommentFunction {
  case class IssueCommentReactableKey(issueId: Int, commentId: Int) extends CommentReactableKey {
    override def description: String = s"comment #$commentId of issue #$issueId"
  }
}
