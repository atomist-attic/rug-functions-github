package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch
import com.atomist.source.git.domain.{PullRequestRequest, ReviewComment}
import com.atomist.source.{FileArtifact, StringFileArtifact}
import com.atomist.util.JsonUtils

class MergePullRequestFunctionTest extends GitHubFunctionTest(Token) {

  "MergePullRequestFunction" should "create pull request and merge" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val readme = ghs.getFileContents(repo, owner, "README.md").head
    val newBranchName = "add-multi-files-branch"
    ghs.createBranch(repo, owner, newBranchName, MasterBranch)

    val update = StringFileArtifact(readme.path, "some new content", FileArtifact.DefaultMode, Some(readme.sha))
    ghs.addOrUpdateFile(repo, owner, newBranchName, "test", update)
    val prr = PullRequestRequest("test title", newBranchName, MasterBranch, "test body")
    val pr = ghs.createPullRequest(repo, owner, prr)

    val f = new MergePullRequestFunction
    val response = f.invoke(pr.number, repo, owner, "merge", ApiUrl, Token)
    response.status shouldBe Status.Success

    ghs.deleteRepository(repo, owner)
  }

  it should "create pull request, squash and merge" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val readme = ghs.getFileContents(repo, owner, "README.md").head
    val newBranchName = "add-multi-files-branch"
    ghs.createBranch(repo, owner, newBranchName, MasterBranch)

    val update = StringFileArtifact(readme.path, "some new content", FileArtifact.DefaultMode, Some(readme.sha))
    ghs.addOrUpdateFile(repo, owner, newBranchName, "test", update)

    val newFile = StringFileArtifact("test.txt", "some new content 2")
    ghs.addOrUpdateFile(repo, owner, newBranchName, "new file", newFile)

    val prr = PullRequestRequest("test title", newBranchName, MasterBranch, "test body")
    val pr = ghs.createPullRequest(repo, owner, prr)

    val f = new MergePullRequestFunction
    val response = f.invoke(pr.number, repo, owner, "squash", ApiUrl, Token)
    response.status shouldBe Status.Success

    ghs.deleteRepository(repo, owner)
  }

  it should "create pull request, rebase" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val readme = ghs.getFileContents(repo, owner, "README.md").head
    val newBranchName = "add-multi-files-branch"
    ghs.createBranch(repo, owner, newBranchName, MasterBranch)

    val update = StringFileArtifact(readme.path, "some new content", FileArtifact.DefaultMode, Some(readme.sha))
    ghs.addOrUpdateFile(repo, owner, newBranchName, "test", update)

    val newFile = StringFileArtifact("test.txt", "some new content 2")
    ghs.addOrUpdateFile(repo, owner, newBranchName, "new file", newFile)

    val prr = PullRequestRequest("test title", newBranchName, MasterBranch, "test body")
    val pr = ghs.createPullRequest(repo, owner, prr)

    val f = new MergePullRequestFunction
    val response = f.invoke(pr.number, repo, owner, "rebase", ApiUrl, Token)
    response.status shouldBe Status.Success

    ghs.deleteRepository(repo, owner)
  }
}
