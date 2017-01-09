package com.atomist.rug.commands.github

import com.atomist.rug.spi.Command
import com.atomist.rug.kind.service.ServicesMutableView
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.{EditIssue, PullRequestMerge}
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}

class GitHubCommands extends Command[ServicesMutableView] {

  override def name: String = "github"

  override def nodeTypes: Set[String] = Set("services")

  override def invokeOn(services: ServicesMutableView): Object = {
    new GitHubOperation()
  }

}

class GitHubOperation() {

  def assignIssue(number: Integer, assignee: String, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.setAssignee(assignee)
    editIssue(issue, owner, repo, token)
  }

  def labelIssue(number: Integer, label: String, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.addLabel(label)
    editIssue(issue, owner, repo, token)
  }

  private def editIssue(issue: EditIssue, owner: String, repo: String, token: String): GitHubStatus = {
    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)

    try {
      githubservices.editIssue(repoId, issue)
      GitHubStatus(true)
    }
    catch {
      case e: Exception => GitHubStatus(false)
    }

  }

  def mergePullRequest(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {
    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val pr = githubservices.getPullRequest(repoId, number)

    try {
      githubservices.mergePullRequest(repoId, new PullRequestMerge(number, pr.head.sha))
      GitHubStatus(true)
    }
    catch {
      case e: Exception => GitHubStatus(false)
    }
  }

}

case class GitHubStatus(success: Boolean)



