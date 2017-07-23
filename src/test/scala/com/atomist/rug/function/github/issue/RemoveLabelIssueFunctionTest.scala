package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import .Issue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class RemoveLabelIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "remove label from issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val gHIssue = createIssue(tempRepo, "test issue", "Issue body")

    val f = new AddLabelIssueFunction
    val response = f.invoke(gHIssue.getNumber, tempRepo.getName, tempRepo.getOwnerName, "bug", ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue = JsonUtils.fromJson[Issue](body.get.str.get)
    val labels = issue.labels
    labels should have size 1
    labels(0).name shouldBe "bug"

    val f2 = new RemoveLabelIssueFunction
    val response2 = f2.invoke(gHIssue.getNumber, tempRepo.getName, tempRepo.getOwnerName, "bug", ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body2 = response2.body
    body2 shouldBe defined
    body2.get.str shouldBe defined
    val issue2 = JsonUtils.fromJson[Issue](body2.get.str.get)
    val labels2 = issue2.labels
    labels2 shouldBe empty
  }
}
