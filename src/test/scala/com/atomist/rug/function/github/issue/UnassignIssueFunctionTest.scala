package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Issue
import com.atomist.util.JsonUtils.fromJson

class UnassignIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "unassign issue to user" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)
    Thread.sleep(2000)

    val f = new AssignIssueFunction
    val response = f.invoke(issue.number, repo, "alankstewart", owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue2 = fromJson[Issue](body.get.str.get)
    issue2.assignees should have size 1

    val f2 = new UnassignIssueFunction
    val response2 = f2.invoke(issue2.number, repo, "alankstewart", owner, ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body2 = response2.body
    body2 shouldBe defined
    body2.get.str shouldBe defined
    val issue3 = fromJson[Issue](body2.get.str.get)
    issue3.assignees shouldBe empty
    ghs.deleteRepository(repo, owner)
  }
}
