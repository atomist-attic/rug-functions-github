package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.{Response, Status}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.EditIssue
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}

/**
  * Common for those functions that edit issues
  */
trait GitHubIssueEditor {

  def editIssue(issue: EditIssue, owner: String, repo: String, token: String): Response = {
    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)

    try {
      githubservices.editIssue(repoId, issue)
      Response(Status.Success, Some(s"Successfully edited issue `#${issue.number}` in `$owner/$repo`"))
    }
    catch {
      case e: Exception => Response(Status.Failure, Some(s"Error editing issue `#${issue.number}` in `$owner/$repo`"), None, Some(e.getMessage))
    }
  }
}
