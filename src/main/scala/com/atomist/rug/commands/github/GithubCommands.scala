package com.atomist.rug.commands.github

import java.time.OffsetDateTime
import java.util

import com.atomist.rug.spi.Command
import com.atomist.rug.kind.service.ServicesMutableView
import com.atomist.source.{ArtifactSourceAccessException, SimpleCloudRepoId}
import com.atomist.source.github.domain._
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class GitHubCommands extends Command[ServicesMutableView] {

  override def name: String = "github"

  override def nodeTypes: Set[String] = Set("services")

  override def invokeOn(services: ServicesMutableView): Object = {
    new GitHubOperation()
  }

}

class GitHubOperation() {

  def createIssue(title: String, comment: String, owner: String, repo: String, token: String): GitHubStatus = {
    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val issue = new CreateIssue(title)
    issue.setBody(comment)

    try {
      val newIssue = gitHubServices.createIssue(repoId, issue)
      GitHubStatus(true, s"Successfully created new issue `#${newIssue.number}` in `${owner}/${repo}`")
    }
    catch {
      case e: Exception => GitHubStatus(false, e.getMessage)
    }
  }

  def assignIssue(number: Integer, assignee: String, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.setAssignee(assignee)
    editIssue(issue, owner, repo, token)
  }

  def reopenIssue(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.setState("open")
    editIssue(issue, owner, repo, token)
  }

  def closeIssue(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.setState("closed")
    editIssue(issue, owner, repo, token)
  }

  def labelIssue(number: Integer, label: String, owner: String, repo: String, token: String): GitHubStatus = {
    val issue = new EditIssue(number)
    issue.addLabel(label)
    editIssue(issue, owner, repo, token)
  }

  def commentIssue(number: Integer, comment: String, owner: String, repo: String, token: String): GitHubStatus = {
    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val issueComment = new IssueComment(number, comment)

    try {
      val newComment = gitHubServices.createIssueComment(repoId, issueComment)
      GitHubStatus(true, s"Successfully created new comment on issue `#${issueComment.num}` in `${owner}/${repo}`")
    }
    catch {
      case e: Exception => GitHubStatus(false, e.getMessage)
    }
  }

  private val templateOpen =
    """{"author_icon":"http://images.atomist.com/rug/issue-open.png","author_link":"%s","author_name":"%s","title":"#%s: %s","title_link":"%s","color":"#2ab27b","ts":%s}""".stripMargin

  private val templateClosed =
    """{"author_icon":"http://images.atomist.com/rug/issue-closed.png","author_link":"%s","author_name":"%s","title":"#%s: %s","title_link":"%s","color":"#D04437","ts":%s}""".stripMargin

  def listIssues(days: Long = 1, token: String): GitHubStatus = {
    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val li = new ListIssues
    li.setDirection("desc")
    li.setSort("updated")
    li.setFilter("assigned")
    li.setState("open")

    val time: OffsetDateTime = days match {
      case e => OffsetDateTime.now.minusDays(e)
      case _ => OffsetDateTime.now.minusDays(1)
    }

    val cri = SimpleCloudRepoId(null, null)
    var issues = new ListBuffer[Issue]
    issues ++= gitHubServices.listIssuesForUser(cri, li).asScala
    li.setState("closed")
    issues ++= gitHubServices.listIssuesForUser(cri, li).asScala

    val message = issues.filter(i => i.updatedAt.isAfter(time))
      .sortWith((i1, i2) => i2.updatedAt.compareTo(i1.updatedAt) > 0)
      .toList.map(i => {
      val id = i.number
      val title = i.title
      // https://api.github.com/repos/octocat/Hello-World/issues/1347
      val url = i.url.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/${i.number}", "")
      // https://github.com/atomisthq/bot-service/issues/72
      val issueUrl = i.url.replace("https://api.github.com/repos/", "https://github.com/")
      // atomisthq/bot-service
      val repo = i.url.replace("https://api.github.com/repos/", "").replace(s"/issues/${i.number}", "")
      val ts = i.updatedAt.toEpochSecond
      i.state match {
        case "closed" => String.format(templateClosed, url, repo, s"$id", title, issueUrl, s"$ts")
        case _ =>  String.format(templateOpen, url, repo, s"$id", title, issueUrl, s"$ts")
      }
    }).mkString(",")

    GitHubStatus(true, s"""{"attachments": [${message}] }""")
  }

  def mergePullRequest(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {
    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)
    val pr = gitHubServices.getPullRequest(repoId, number)

    try {
      gitHubServices.mergePullRequest(repoId, new PullRequestMerge(number, pr.head.sha))
      GitHubStatus(true, s"Successfully merged pull request `${pr.number}`")
    }
    catch {
      case e: Exception => GitHubStatus(false, e.getMessage)
    }
  }

  def createRelease(tagName: String, owner: String, repo: String, token: String): GitHubStatus = {
    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo);

    var tags: util.List[TagInfo] = null
    var tag: Option[String] = Option.empty

    if (tagName != null && tagName.length == 0) {
      try {
        tags = gitHubServices.listTags(repoId);
      } catch {
        case e: ArtifactSourceAccessException =>
          return GitHubStatus(false, e.message)
      }
      if (tags != null && !tags.isEmpty()) {
        tag = Option.apply(JavaConversions.asScalaBuffer(tags).head.name)
      }
    }

    if (tag.isEmpty) {
      return GitHubStatus(false, s"No tag found in `${owner}/${repo}`")
    }

    try {
      val release = gitHubServices.createRelease(repoId, new CreateRelease(tag.get, "master", null, null, false, false))
      return GitHubStatus(true, s"Successfully created release `${release.tagName}` in `${owner}/${repo}#${release.targetCommitish}`")
    } catch {
      case e: ArtifactSourceAccessException =>
        return GitHubStatus(false, e.message)
    }
  }

  private def editIssue(issue: EditIssue, owner: String, repo: String, token: String): GitHubStatus = {
    val githubservices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)

    try {
      githubservices.editIssue(repoId, issue)
      GitHubStatus(true, s"Successfully edited issue `#${issue.number}` in `${owner}/${repo}`")
    }
    catch {
      case e: Exception => GitHubStatus(false, e.getMessage)
    }
  }

}

case class GitHubStatus(success: Boolean, message: String = "")



