package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.function.github.issue.GitHubIssues.GitHubIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class SearchIssuesFunctionTest extends GitHubFunctionTest(Token) {

  it should "search issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")
    issue.addAssignees(ghs.gitHub.getUser("alankstewart"))
    Thread.sleep(2000)

    val f = new SearchIssuesFunction
    val response = f.invoke(null, tempRepo.getName, tempRepo.getOwnerName, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues should have size 1
    val issue1 = issues.head
    issue1.title shouldBe "test issue"
    issue1.state shouldBe "OPEN"
  }

  it should "fail to search issues in non-existent repo" in {
    val f = new SearchIssuesFunction
    val response = f.invoke(null, "github-commands", "comfoobar", ApiUrl, Token)
    response.status shouldBe Status.Failure
  }
}
