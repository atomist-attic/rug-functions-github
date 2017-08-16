package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class SearchIssuesFunctionTest extends GitHubFunctionTest(Token) {

  "SearchIssuesFunction" should "search issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    createIssue(repo, owner)
    Thread.sleep(2000)

    val f = new SearchIssuesFunction
    val response = f.invoke("", 1, 30, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val bodyStr = body.get.str.get
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](bodyStr)
    issues should have size 1
    val issue1 = issues.head
    issue1.title shouldBe "test issue"
    issue1.state shouldBe "open"
    ghs.deleteRepository(repo, owner)
  }
}
