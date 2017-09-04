package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status

class CommentIssueFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  "CommentIssueFunction" should "add comment to issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new CommentIssueFunction
    val response = f.invoke(issue.number, s"this is a comment #${issue.number}", repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    ghs.deleteRepository(repo, owner)
  }

  it should "fail to add comment to unknown issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val f = new CommentIssueFunction
    val response = f.invoke(999, "this is a comment", repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Failure
    ghs.deleteRepository(repo, owner)
  }
}
