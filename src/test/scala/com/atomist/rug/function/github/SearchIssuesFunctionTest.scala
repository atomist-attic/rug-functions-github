package com.atomist.rug.function.github

import org.scalatest.{FlatSpec, Matchers}

class SearchIssuesFunctionTest extends FlatSpec with Matchers {

  it should "search issues" in {
    val sif = new SearchIssuesFunction
    val response = sif.invoke(null, "github-commands", "atomisthqa", TestCredentials.Token)
    val body = response.body.orNull
    body shouldNot be(null)
    println(body)
  }
}
