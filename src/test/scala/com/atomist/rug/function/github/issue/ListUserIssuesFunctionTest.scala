package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class ListUserIssuesFunctionTest extends GitHubFunctionTest(Token) {

  it should "list issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new AssignIssueFunction
    val response = f.invoke(issue.number, repo, "alankstewart", owner, ApiUrl, Token)
    response.status shouldBe Status.Success

    val f2 = new ListUserIssuesFunction
    val response2 = f2.invoke("1", Token)
    response2.status shouldBe Status.Success
    val body = response2.body
    body shouldBe defined
    body.get.str shouldBe defined
    val bodyStr = body.get.str.get
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](bodyStr)
    issues.size should be > 0
  }
}
