package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.function.github.reaction.GitHubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.github.domain.ReactionContent
import com.atomist.util.JsonUtils

class ReactIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new ReactIssueFunction
    val response = f.invoke("+1", issue.number, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Reaction](response.body.get.str.get)
    result.content shouldBe "+1"

    val actualReactions = ghs listIssueReactions(repo, owner, issue.number, Some(ReactionContent.withName(result.content)))
    actualReactions.size shouldBe 1
    actualReactions.head.content shouldBe ReactionContent.PlusOne
  }
}
