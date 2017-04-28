package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunctionTest
import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.github.GitHubServices.ReviewComment
import com.atomist.util.JsonUtils

import scala.collection.JavaConverters._

class CreateReviewCommentFunctionTest extends GitHubFunctionTest(Token) {

  it should "create review comment" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val sha1 = tempRepo.listCommits.asScala.toSeq.head.getSHA1
    tempRepo.createRef("refs/heads/test", sha1)
    val readme = tempRepo.getFileContent("README.md")
    val update = readme.update("test content", "test commit", "test")
    val pr = tempRepo.createPullRequest("test title", "test", "master", "test body")

    val f = new CreateReviewCommentFunction
    val response = f.invoke(pr.getNumber, "comment body", pr.getHead.getSha, "README.md", 1, tempRepo.getName, tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val reviewComment = JsonUtils.fromJson[ReviewComment](body.get.str.get)
    reviewComment.body should equal("comment body")
  }
}
