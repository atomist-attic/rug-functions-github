package com.atomist.rug.function.github

object TestConstants {

  val Token = System.getenv("GITHUB_TEST_TOKEN")

  val TestUser = System.getenv("GITHUB_TEST_USER")

  val ApiUrl = "https://api.github.com"

  val TemporaryRepoPrefix = "TEST_CAN_DELETE_"

  val TestWebHookUrlBase = "http://rug-functions-github-test.atomist.com/webhook/"

  /**
    * We will create and delete repos here.
    */
  val TestOrg = "atomisthqtest"
}
