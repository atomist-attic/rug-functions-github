package com.atomist.rug.commands.github

import com.atomist.rug.spi.Command
import com.atomist.rug.kind.service.ServicesMutableView
import com.atomist.source.SimpleCloudRepoId
import com.atomist.source.github.domain.EditIssue
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

    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val issue = new EditIssue(number)
    issue.setAssignee(assignee)

    try {
      githubservices.editIssue(repoId, issue)
      GitHubStatus(true)
    }
    catch {
      case e: Exception => GitHubStatus(false)
    }
  }

}

case class GitHubStatus(success: Boolean)



