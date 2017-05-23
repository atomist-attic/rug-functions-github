package com.atomist.rug.function.github

object TestConstants {

  val Token = System.getenv("GITHUB_TEST_TOKEN")

  val TemporaryRepoPrefix = "TEST_CAN_DELETE_"

  /**
    * We will create and delete repos here.
    */
  val TestOrg = "atomisthqtest"
}
