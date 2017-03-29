package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestCredentials._
import com.atomist.rug.spi.Handlers.Status

import scala.collection.JavaConverters._

class ReactPullRequestFunctionTest extends GitHubFunctionTest(Token) {

  it should "currently be unable to add reaction to pull request" in {
    // when this capability works on GitHub, a failing test here will let us know
    val tempRepo = newPopulatedTemporaryRepo()
    val sha1 = tempRepo.listCommits.asScala.toSeq.head.getSHA1
    tempRepo.createRef("refs/heads/test", sha1)
    val readme = tempRepo.getFileContent("README.md")
    val update = readme.update("test content", "test commit", "test")
    val pullRequest = tempRepo.createPullRequest("test title", "test", "master", "test body")

    val f = new ReactPullRequestFunction
    val response = f.invoke("+1", pullRequest.getNumber, tempRepo.getName, tempRepo.getOwnerName, Token)

    // reading and writing reactions on Pull Requests is not supported by GitHub today
    response.status shouldBe Status.Failure
    intercept[Error] {
      pullRequest.listReactions().asScala.toSeq
    }
  }
}
