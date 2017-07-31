package com.atomist.rug.function.github

import com.atomist.source.ArtifactSourceException
import com.atomist.source.git.github.GitHubServices
import com.atomist.source.git.github.domain.Issue
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

abstract class GitHubFunctionTest(val oAuthToken: String, val apiUrl: String = "")
  extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with LazyLogging {

  import TestConstants._

  protected val ghs = GitHubServices(oAuthToken, apiUrl)

  override protected def afterAll(): Unit = cleanUp()

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

  /**
    * Clean up after the work of this class.
    */
  private def cleanUp() =
    Try(ghs.searchRepositories(Map("q" -> s"user:$TestOrg in:name $TemporaryRepoPrefix", "per_page" -> "100"))) match {
      case Success(repos) => repos.items.foreach(repo => ghs.deleteRepository(repo.name, repo.ownerName))
      case Failure(e) => throw ArtifactSourceException(e.getMessage, e)
    }

  private def getRepoName = s"$TemporaryRepoPrefix${System.nanoTime}"

  protected def createIssue(repo: String, owner: String): Issue =
    ghs.createIssue(repo, owner, "test issue", "Issue body")
}
