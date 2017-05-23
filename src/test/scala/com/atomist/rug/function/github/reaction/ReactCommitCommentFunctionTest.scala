package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.kohsuke.github.ReactionContent

import scala.collection.JavaConverters._

class ReactCommitCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to commit comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val commit = tempRepo.listCommits().asScala.toSeq.head
    val commitComment = commit.createComment("test comment")

    val f = new ReactCommitCommentFunction
    val response = f.invoke("+1", commit.getSHA1, commitComment.getId, tempRepo.getName, tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Map[String, Any]](response.body.get.str.get)
    result("content") shouldBe "+1"

    val actualReactions = commitComment.listReactions().asScala.toSeq
    actualReactions.size shouldBe 1
    actualReactions.head.getContent shouldBe ReactionContent.PLUS_ONE
  }
}
