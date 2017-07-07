package com.atomist.rug.function.github

object TestConstants {

  val Token = System.getenv("GITHUB_TOKEN")

  val ApiUrl = "https://api.github.com"

  val TemporaryRepoPrefix = "TEST_CAN_DELETE_"

  /**
    * We will create and delete repos here.
    */
  val TestOrg = "atomisthqtest"
}
