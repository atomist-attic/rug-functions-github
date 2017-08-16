package com.atomist.rug.function.github

import com.atomist.source.git.GitHubServices
import com.atomist.source.git.domain.Issue
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}

abstract class GitHubFunctionTest(val oAuthToken: String, val apiUrl: String = "")
  extends FlatSpec
    with Matchers
    with LazyLogging {

  import TestConstants._

  protected val ghs = GitHubServices(oAuthToken, apiUrl)

  protected def testWebHookUrl = TestWebHookUrlBase + java.util.UUID.randomUUID.toString

  private val repoNamePrefix = TemporaryRepoPrefix + java.util.UUID.randomUUID.toString + "_"

  /**
    * Return a temporary repository callers can use.
    */
  def newTemporaryRepo(autoInit: Boolean = false) =
    ghs.createRepository(getRepoName, TestOrg, "temporary test repository", privateFlag = true, autoInit = autoInit)

  /**
    * Most callers will want a repository with something in it. Otherwise there isn't even a default branch,
    * so put in a README.md file by setting auto_init to true.
    */
  def newPopulatedTemporaryRepo() = newTemporaryRepo(true)

  private def getRepoName = s"$repoNamePrefix${System.nanoTime}"

  protected def createIssue(repo: String, owner: String): Issue =
    ghs.createIssue(repo, owner, "test issue", "Issue body")
}
