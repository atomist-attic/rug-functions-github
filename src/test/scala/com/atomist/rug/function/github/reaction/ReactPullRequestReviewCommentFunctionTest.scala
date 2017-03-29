package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestCredentials._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.kohsuke.github.ReactionContent

import scala.collection.JavaConverters._

class ReactPullRequestReviewCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to pull request review comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val sha1 = tempRepo.listCommits.asScala.toSeq.head.getSHA1
    tempRepo.createRef("refs/heads/test", sha1)
    val readme = tempRepo.getFileContent("README.md")
    val update = readme.update("test content", "test commit", "test")
    val pullRequest = tempRepo.createPullRequest("test title", "test", "master", "test body")
    val comment = pullRequest.createReviewComment("test body", update.getCommit.getSHA1, readme.getPath, 1)

    val f = new ReactPullRequestReviewCommentFunction
    val response = f.invoke("+1", pullRequest.getNumber, comment.getId, tempRepo.getName, tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Map[String, Any]](response.body.get.str.get)
    result("content") shouldBe "+1"

    val actualReactions = comment.listReactions().asScala.toSeq
    actualReactions.size shouldBe 1
    actualReactions.head.getContent shouldBe ReactionContent.PLUS_ONE
  }
}
