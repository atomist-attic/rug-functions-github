package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.GitArtifactSourceLocator.MasterBranch

class DeleteBranchFunctionTest extends GitHubFunctionTest(Token) {

  it should "delete branch" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val branchName = "foobar"
    val ref = ghs.createBranch(repo, owner, branchName, MasterBranch)
    ref.`object`.sha should not be empty

    val f = new DeleteBranchFunction
    val response = f.invoke(branchName, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    ghs.getBranch(repo, owner, branchName) shouldBe empty
    ghs.deleteRepository(repo, owner)
  }
}
