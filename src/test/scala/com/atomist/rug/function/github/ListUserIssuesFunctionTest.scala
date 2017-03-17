package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubSearchIssues.GitHubIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.scalatest.{FlatSpec, Matchers}

class ListUserIssuesFunctionTest extends FlatSpec with Matchers {

  it should "list issues" in {
    val sif = new ListUserIssuesFunction
    val response = sif.invoke("31", TestCredentials.Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues.size shouldBe >(0)
  }

  it should "fail to list issues with bad token" in {
    val sif = new ListUserIssuesFunction
    val response = sif.invoke("1", "comfoobar")
    response.status shouldBe Status.Failure
  }
}
