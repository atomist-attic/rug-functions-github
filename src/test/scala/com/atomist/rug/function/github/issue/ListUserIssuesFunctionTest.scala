package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.function.github.issue.GitHubIssues.GitHubIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class ListUserIssuesFunctionTest extends GitHubFunctionTest(Token) {

  it should "list issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")
    issue.addAssignees(ghs.gitHub.getUser("alankstewart"))

    val f = new ListUserIssuesFunction
    val response = f.invoke("1", Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues.size should be > 0
  }

  it should "fail to list issues with bad token" in {
    val f = new ListUserIssuesFunction
    val response = f.invoke("1", "comfoobar")
    response.status shouldBe Status.Failure
  }
}
