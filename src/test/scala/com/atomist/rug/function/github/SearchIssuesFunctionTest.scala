package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubSearchIssues.GitHubIssue
import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class SearchIssuesFunctionTest extends GitHubFunctionTest(Token) {

  it should "search issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")
    issue.addAssignees(gitHub.getUser("alankstewart"))

    val f = new SearchIssuesFunction
    val response = f.invoke(null, tempRepo.getName, tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues should have size 1
    issues.head.title shouldBe "test issue"
  }

  it should "fail to search issues in non-existent repo" in {
    val f = new SearchIssuesFunction
    val response = f.invoke(null, "github-commands", "comfoobar", Token)
    response.status shouldBe Status.Failure
  }
}
