package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.reaction.CreateReactionFunction.ReactableKey
import com.atomist.rug.function.github.reaction.ReactIssueFunction.IssueReactableKey
import com.atomist.rug.spi.FunctionResponse
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import org.kohsuke.github.GHRepository

/**
  * Reacts to a  GitHub issue
  */
class ReactIssueFunction extends CreateReactionFunction[IssueReactableKey] {

  override def retrieveReactable(repository: GHRepository, reactableKey: IssueReactableKey): Reactable = {
    repository.getIssue(reactableKey.issueId)
  }

  @RugFunction(name = "react-github-issue", description = "Reacts to a GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "issue") issueId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
      createReaction(reaction, IssueReactableKey(issueId), repo, owner, token, apiUrl)
  }

}

object ReactIssueFunction {
  case class IssueReactableKey(issueId: Int) extends ReactableKey {
    override def description: String = s"issue #$issueId"
  }
}