package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.{GitHubFunctionTest, TestConstants}
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.StringFileArtifact
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch
import com.atomist.util.JsonUtils

class ListUserIssuesFunctionTest extends GitHubFunctionTest(Token) {

  "ListUserIssuesFunction" should "list issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    ghs.addOrUpdateFile(repo, owner, MasterBranch, s"new file 1 #${issue.number}", StringFileArtifact("src/test.txt", "some text"))
    ghs.addOrUpdateFile(repo, owner, MasterBranch, s"new file 2 #${issue.number}", StringFileArtifact("src/test2.txt", "some other text"))

    val f = new AssignIssueFunction
    val response = f.invoke(issue.number, repo, TestConstants.TestUser, owner, ApiUrl, Token)
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
    issues.find(i => i.repo == s"$owner/$repo") match {
      case Some(i) => i.commits.size shouldEqual 2
      case None => fail("Failed to find issue")
    }
    ghs.deleteRepository(repo, owner)
  }
}
