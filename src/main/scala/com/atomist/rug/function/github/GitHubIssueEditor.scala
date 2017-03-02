package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.EditIssue
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}

/**
  * Common for those functions that edit issues
  */
trait GitHubIssueEditor {

  def editIssue(issue: EditIssue, owner: String, repo: String, token: String): FunctionResponse = {
    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = SimpleCloudRepoId(owner, repo)

    try {
      val res = githubservices.editIssue(repoId, issue)
      FunctionResponse(Status.Success, Some(s"Successfully edited issue `#${issue.number}` in `$owner/$repo`"), None, JsonBodyOption(res))
    }
    catch {
      case e: Exception => FunctionResponse(Status.Failure, Some(s"Error editing issue `#${issue.number}` in `$owner/$repo`"), None, StringBodyOption(e.getMessage))
    }
  }
}
