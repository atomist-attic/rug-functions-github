package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class CloseIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "close issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new CloseIssueFunction
    val response = f.invoke(issue.number, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    Thread.sleep(3000)

    val f2 = new SearchIssuesFunction
    val response2 = f2.invoke(null, 1, 30, repo, owner, ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body = response2.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues shouldBe empty
    ghs.deleteRepository(repo, owner)
  }
}
