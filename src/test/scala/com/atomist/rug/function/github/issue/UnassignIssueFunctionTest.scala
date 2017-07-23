package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import .Issue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class UnassignIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "unassign issue to user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")

    val f = new AssignIssueFunction
    val response = f.invoke(issue.getNumber, tempRepo.getName, "alankstewart", tempRepo.getOwnerName, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue2 = JsonUtils.fromJson[Issue](body.get.str.get)
    issue2.assignees shouldBe defined

    val f2 = new UnassignIssueFunction
    val response2 = f2.invoke(issue.getNumber, tempRepo.getName, "alankstewart", tempRepo.getOwnerName, ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body2 = response2.body
    body2 shouldBe defined
    body2.get.str shouldBe defined
    val issue3 = JsonUtils.fromJson[Issue](body2.get.str.get)
    issue3.assignees shouldBe empty
  }
}
