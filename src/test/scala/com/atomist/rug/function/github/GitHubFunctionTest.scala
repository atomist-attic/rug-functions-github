package com.atomist.rug.function.github

import com.atomist.source.{ArtifactSourceAccessException, ArtifactSourceCreationException, ArtifactSourceUpdateException}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHIssue, GHRepository, GitHub}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

abstract class GitHubFunctionTest(val oAuthToken: String, val apiUrl: String = "")
  extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with LazyLogging {

  import TestConstants._

  protected lazy val gitHub = apiUrl match {
    case url if url != null && !url.isEmpty => GitHub.connectToEnterprise(url, oAuthToken)
    case _ => GitHub.connectUsingOAuth(oAuthToken)
  }

  override protected def afterAll(): Unit = cleanUp()

  /**
    * Return a temporary repository callers can use.
    */
  def newTemporaryRepo(autoInit: Boolean = false) = {
    Try(gitHub.getOrganization(TestOrg).createRepository(getRepoName)
      .private_(true)
      .description("temporary test repository")
      .issues(true)
      .autoInit(autoInit)
      .create()) match {
      case Success(repo) =>
        repo should not be null
        repo.getDescription shouldEqual "temporary test repository"
        repo
      case Failure(e) => throw ArtifactSourceCreationException(e.getMessage, e)
    }
  }

  /**
    * Most callers will want a repository with something in it. Otherwise there isn't even a default branch,
    * so put in a README.md file by setting auto_init to true.
    */
  def newPopulatedTemporaryRepo() = newTemporaryRepo(true)

  /**
    * Clean up after the work of this class.
    */
  private def cleanUp() =
    Try(gitHub.searchRepositories().q(s"user:$TestOrg in:name $TemporaryRepoPrefix").list) match {
      case Success(repos) => repos.asScala.foreach(_.delete)
      case Failure(e) => throw ArtifactSourceAccessException(e.getMessage, e)
    }

  private def getRepoName = s"$TemporaryRepoPrefix${System.nanoTime}"

  protected def createIssue(repository: GHRepository, title: String, body: String): GHIssue =
    Try(repository.createIssue(title).body(body).create()) match {
      case Success(issue) => issue
      case Failure(e) => throw ArtifactSourceUpdateException(e.getMessage, e)
    }
}
