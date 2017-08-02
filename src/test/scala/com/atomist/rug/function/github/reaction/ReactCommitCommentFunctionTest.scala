package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.function.github.reaction.GitHubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.ReactionContent
import com.atomist.util.JsonUtils

class ReactCommitCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to commit comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val commit = ghs.listCommits(repo, owner).head
    val comment = ghs.createCommitComment(repo, owner, commit.sha, "test comment", "README.md", 1)

    val f = new ReactCommitCommentFunction
    val response = f.invoke("+1", commit.sha, comment.id, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Reaction](response.body.get.str.get)
    result.content shouldBe "+1"

    val actualReactions = ghs.listCommitCommentReactions(repo, owner, comment.id, Some(ReactionContent.withName(result.content)))
    actualReactions.size shouldBe 1
    actualReactions.head.content shouldBe ReactionContent.PlusOne
  }
}
