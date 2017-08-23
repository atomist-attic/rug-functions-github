package com.atomist.rug.function.github.issue

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.StringFileArtifact
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch
import com.atomist.util.JsonUtils

class SearchIssuesFunctionTest extends GitHubFunctionTest(Token) {

  "SearchIssuesFunction" should "search issues" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val issue = createIssue(repo, owner)

    ghs.addOrUpdateFile(repo, owner, MasterBranch, s"new file 1 #${issue.number}", StringFileArtifact("src/test.txt", "some text"))
    ghs.addOrUpdateFile(repo, owner, MasterBranch, s"new file 2 #${issue.number}", StringFileArtifact("src/test2.txt", "some other text"))

    val f = new SearchIssuesFunction
    val response = f.invoke("", 1, 30, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val bodyStr = body.get.str.get
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](bodyStr)
    issues.size should be > 0
    issues.find(i => i.repo == s"$owner/$repo") match {
      case Some(i) =>
        i.title shouldBe "test issue"
        i.state shouldBe "open"
        i.commits.size should be > 0
      case None => fail("Failed to find issue")
    }

    ghs.deleteRepository(repo, owner)
  }
}
