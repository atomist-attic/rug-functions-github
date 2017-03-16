package com.atomist.rug.function.github

import com.atomist.util.JsonUtils
import org.scalatest.{FlatSpec, Matchers}

class SearchIssuesFunctionTest extends FlatSpec with Matchers {

  it should "search issues" in {
    val sif = new SearchIssuesFunction
    val response = sif.invoke(null, "github-commands", "atomisthqa", TestCredentials.Token)
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues.size shouldBe 1
  }
//
//  it should "fail to search issues in non-existent repo" in pendingUntilFixed {
//    val sif = new SearchIssuesFunction
//    val response = sif.invoke(null, "github-commands", "atomisthq", TestCredentials.Token)
//    val body = response.body
//    body shouldBe defined
//    body.get.str shouldBe defined
//  }
}
