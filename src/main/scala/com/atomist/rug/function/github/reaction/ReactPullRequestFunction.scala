package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.reaction.CreateReactionFunction.ReactableKey
import com.atomist.rug.function.github.reaction.ReactPullRequestFunction.PullRequestReactableKey
import com.atomist.rug.spi.FunctionResponse
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import org.kohsuke.github.GHRepository

/**
  * Reacts to a GitHub pull request
  */
class ReactPullRequestFunction extends CreateReactionFunction[PullRequestReactableKey] {

  override def retrieveReactable(repository: GHRepository, reactableKey: PullRequestReactableKey): Reactable = {
    // must look pull request up as an Issue to add reactions
    repository.getIssue(reactableKey.pullRequestId)
  }

  @RugFunction(name = "react-github-pull-request", description = "Reacts to a GitHub pull request",
    tags = Array(new Tag(name = "github"), new Tag(name = "pull requests"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "pullRequest") pullRequestId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
      createReaction(reaction, PullRequestReactableKey(pullRequestId), repo, owner, token, apiUrl)
  }

}

object ReactPullRequestFunction {
  case class PullRequestReactableKey(pullRequestId: Int) extends ReactableKey {
    override def description: String = s"pull request #$pullRequestId"
  }
}
