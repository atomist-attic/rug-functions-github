package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubSearchIssues.GitHubIssue
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils
import org.scalatest.{FlatSpec, Matchers}

class SearchIssuesFunctionTest extends FlatSpec with Matchers {

  it should "search issues" in {
    val sif = new SearchIssuesFunction
    val response = sif.invoke(null, "github-commands", "atomisthqa", TestCredentials.Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues.size shouldBe >(0)
  }

  it should "fail to search issues in non-existent repo" in {
    val sif = new SearchIssuesFunction
    val response = sif.invoke(null, "github-commands", "atomisthq", TestCredentials.Token)
    response.status shouldBe Status.Failure
  }
}