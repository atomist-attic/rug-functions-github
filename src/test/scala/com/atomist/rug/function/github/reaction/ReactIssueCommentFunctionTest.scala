package com.atomist.rug.function.github.reaction

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestConstants._
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.kohsuke.github.ReactionContent

import scala.collection.JavaConverters._

class ReactIssueCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "add reaction to issue comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")
    val issueComment = issue.comment("test comment")

    val f = new ReactIssueCommentFunction
    val response = f.invoke("+1", issue.getNumber, issueComment.getId, tempRepo.getName, tempRepo.getOwnerName, Token, ApiUrl)
    response.status shouldBe Status.Success
    val result = JsonUtils.fromJson[Map[String, Any]](response.body.get.str.get)
    result("content") shouldBe "+1"

    val actualReactions = issueComment.listReactions().asScala.toSeq
    actualReactions.size shouldBe 1
    actualReactions.head.getContent shouldBe ReactionContent.PLUS_ONE
  }
}
