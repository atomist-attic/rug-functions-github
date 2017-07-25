package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch
import com.atomist.source.git.github.domain.{PullRequestRequest, ReviewComment}
import com.atomist.source.{FileArtifact, StringFileArtifact}
import com.atomist.util.JsonUtils

class CreateReviewCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "create pull request review comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val readme = ghs.getFileContents(repo, owner, "README.md").head
    val newBranchName = "add-multi-files-branch"
    ghs createBranch(repo, owner, newBranchName, MasterBranch)

    val update = StringFileArtifact(readme.path, "some new content", FileArtifact.DefaultMode, Some(readme.sha))
    ghs.addOrUpdateFile(repo, owner, newBranchName, "test", update)
    val prr = PullRequestRequest("test title", newBranchName, MasterBranch, "test body")
    val pr = ghs.createPullRequest(repo, owner, prr)

    val f = new CreateReviewCommentFunction
    val response = f.invoke(pr.number, "comment body", pr.head.sha, "README.md", 1, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val reviewComment = JsonUtils.fromJson[ReviewComment](body.get.str.get)
    reviewComment.body should equal("comment body")
  }
}
