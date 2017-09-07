package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Issue
import com.atomist.util.JsonUtils

class ToggleIssueLabelFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  "ToggleIssueLabelFunction" should "add label to and remove label from an issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new ToggleIssueLabelFunction
    val response = f.invoke(issue.number, repo, owner, "bug", ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue1 = JsonUtils.fromJson[Issue](body.get.str.get)
    val labels = issue1.labels
    labels should have size 1
    labels(0).name shouldBe "bug"

    val response2 = f.invoke(issue.number, repo, owner, "bug", ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body2 = response2.body
    body2 shouldBe defined
    body2.get.str shouldBe defined
    val issue2 = JsonUtils.fromJson[Issue](body2.get.str.get)
    val labels2 = issue2.labels
    labels2 shouldBe empty

    ghs.deleteRepository(repo, owner)
  }

  it should "add two labels and remove one label" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    val f = new ToggleIssueLabelFunction
    val response = f.invoke(issue.number, repo, owner, "bug", ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issue1 = JsonUtils.fromJson[Issue](body.get.str.get)
    val labels = issue1.labels
    labels should have size 1
    labels(0).name shouldBe "bug"

    val response2 = f.invoke(issue.number, repo, owner, "question", ApiUrl, Token)
    response2.status shouldBe Status.Success
    val body2 = response2.body
    body2 shouldBe defined
    body2.get.str shouldBe defined
    val issue2 = JsonUtils.fromJson[Issue](body2.get.str.get)
    val labels2 = issue2.labels
    labels2 should have size 2
    labels2.map(_.name) should contain allOf("bug", "question")

    val response3 = f.invoke(issue.number, repo, owner, "bug", ApiUrl, Token)
    response3.status shouldBe Status.Success
    val body3 = response3.body
    body3 shouldBe defined
    body3.get.str shouldBe defined
    val issue3 = JsonUtils.fromJson[Issue](body3.get.str.get)
    val labels3 = issue3.labels
    labels3 should have size 1
    labels3(0).name shouldBe "question"

    ghs.deleteRepository(repo, owner)
  }

  it should "fail to add label to unknown issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val f = new ToggleIssueLabelFunction
    val issueNumber = 999
    val response = f.invoke(issueNumber, repo, owner, "bug", ApiUrl, Token)
    response.status shouldBe Status.Failure
    val body = response.msg
    body shouldBe defined
    body.get shouldEqual s"Failed to find issue `#$issueNumber` in `$owner/$repo`"

    ghs.deleteRepository(repo, owner)
  }
}
