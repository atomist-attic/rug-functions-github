package com.atomist.rug.function.github

import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.spi.Handlers.Status

class AssignIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "assign issue to user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")

    val f = new AssignIssueFunction
    val response = f.invoke(issue.getNumber, tempRepo.getName, "alankstewart", tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success
  }

  it should "fail to assign issue with unknown user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")

    val f = new AssignIssueFunction
    val response = f.invoke(issue.getNumber, tempRepo.getName, "comfoobar", tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Failure
  }
}
