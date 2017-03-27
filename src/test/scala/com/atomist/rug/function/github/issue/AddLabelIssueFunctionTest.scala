package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.function.github.issue.GitHubIssues.Issue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class AddLabelIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "add label to issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val gHIssue = createIssue(tempRepo, "test issue", "Issue body")

    val f = new AddLabelIssueFunction
    val response = f.invoke(gHIssue.getNumber, tempRepo.getName, tempRepo.getOwnerName, "bug", Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue = JsonUtils.fromJson[Issue](body.get.str.get)
    val labels = issue.labels
    labels should have size 1
    labels(0).name shouldBe "bug"
  }
}
