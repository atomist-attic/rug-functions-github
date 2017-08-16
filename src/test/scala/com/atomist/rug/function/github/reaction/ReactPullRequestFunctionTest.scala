package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.function.github.reaction.GitHubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch
import com.atomist.source.git.domain.{PullRequestRequest, ReactionContent}
import com.atomist.source.{FileArtifact, StringFileArtifact}
import com.atomist.util.JsonUtils

class ReactPullRequestFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to pull request" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val readme = ghs.getFileContents(repo, owner, "README.md").head
    val newBranchName = "add-multi-files-branch"
    ghs.createBranch(repo, owner, newBranchName, MasterBranch)

    val update = StringFileArtifact(readme.path, "some new content", FileArtifact.DefaultMode, Some(readme.sha))
    ghs.addOrUpdateFile(repo, owner, newBranchName, "test", update)

    val prr = PullRequestRequest("test title", newBranchName, MasterBranch, "test body")
    val pr = ghs.createPullRequest(repo, owner, prr)

    val f = new ReactPullRequestFunction
    val response = f.invoke("+1", pr.number, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Reaction](response.body.get.str.get)
    result.content shouldBe "+1"

    val actualReactions = ghs.listPullRequestReactions(repo, owner, pr.number, Some(ReactionContent.withName(result.content)))
    actualReactions.size shouldBe 1
    actualReactions.head.content shouldBe ReactionContent.PlusOne
    ghs.deleteRepository(repo, owner)
  }
}
