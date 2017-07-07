package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.kohsuke.github.ReactionContent

import scala.collection.JavaConverters._

class ReactPullRequestFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to pull request" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val sha1 = tempRepo.listCommits.asScala.toSeq.head.getSHA1
    tempRepo.createRef("refs/heads/test", sha1)
    val readme = tempRepo.getFileContent("README.md")
    val update = readme.update("test content", "test commit", "test")
    val pullRequest = tempRepo.createPullRequest("test title", "test", "master", "test body")

    val f = new ReactPullRequestFunction
    val response = f.invoke("+1", pullRequest.getNumber, tempRepo.getName, tempRepo.getOwnerName, Token, ApiUrl)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Map[String, Any]](response.body.get.str.get)
    result("content") shouldBe "+1"

    // must look pull request up as an Issue to see reactions
    val prAsIssue = tempRepo.getIssue(pullRequest.getNumber)
    val actualReactions = prAsIssue.listReactions().asScala.toSeq
    actualReactions.size shouldBe 1
    actualReactions.head.getContent shouldBe ReactionContent.PLUS_ONE
  }
}
