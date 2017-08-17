package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status

class AssignIssueFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  it should "assign issue to user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new AssignIssueFunction
    val response = f.invoke(issue.number, repo, "alankstewart", owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    ghs.deleteRepository(repo, owner)
  }

  it should "fail to assign issue with unknown user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = ghs.createIssue(repo, owner, "test issue", "Issue body", Seq.empty)

    val f = new AssignIssueFunction
    val response = f.invoke(issue.number, repo, "comfoobar", owner, ApiUrl, Token)
    response.status shouldBe Status.Failure
    ghs.deleteRepository(repo, owner)
  }
}
