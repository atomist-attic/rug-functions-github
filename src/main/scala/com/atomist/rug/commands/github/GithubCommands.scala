package com.atomist.rug.commands.github

import java.net.URL
import java.time.OffsetDateTime
import java.util
import java.util.Collections

import com.atomist.rug.spi.Command
import com.atomist.util.lang.JavaScriptArray
import com.atomist.rug.kind.service.ServicesMutableView
import com.atomist.source.{ArtifactSourceAccessException, SimpleCloudRepoId}
import com.atomist.source.github.domain._
import com.atomist.source.github.{GitHubServices, GitHubServicesImpl}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.{GHEvent, GitHub}

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class GitHubCommands extends Command[ServicesMutableView]
  with LazyLogging {

  val gitHubOperation = new GitHubOperation

  override def name: String = "github"

  override def nodeTypes: java.util.Set[String] = Collections.singleton("Services")

  override def invokeOn(services: ServicesMutableView): Object = {
    gitHubOperation
  }

}

class GitHubOperation extends LazyLogging {

  def installWebhook(url: String, owner: String, repo: String, token: String): GitHubStatus = {
    logger.info(s"Invoking installWebhook with url '$url', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

    val config = new util.HashMap[String, String]()
    config.put("url", url)
    config.put("content_type", "json")
    val events = Seq(GHEvent.ALL)

    if (repo == null || repo.length == 0) {
      try {
        val github = GitHub.connectUsingOAuth(token)
        github.getOrganization(owner).createHook("web", config, events.asJava, true)
        GitHubStatus(true, s"Successfully installed org-level webhook for `${owner}`")
      }
      catch {
        case e: Exception => GitHubStatus(false, e.getMessage)
      }
    }
    else {
      val github = GitHub.connectUsingOAuth(token)
      github.getOrganization(owner).getRepository(repo).createHook("web", config, events.asJava, true)
      GitHubStatus(true, s"Successfully installed repo-level webhook for `${owner}/${repo}`")
    }
  }

  def createIssue(title: String, comment: String, owner: String, repo: String, token: String): GitHubStatus = {

    logger.info(s"Invoking createIssue with title '$title', comment '${comment}', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

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

    logger.info(s"Invoking assignIssue with number '$number', assignee '${assignee}', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'"
    );

    val issue = new EditIssue(number)
    issue.setAssignee(assignee)
    editIssue(issue, owner, repo, token)
  }

  def reopenIssue(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {

    logger.info(s"Invoking reopenIssue with number '$number', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

    val issue = new EditIssue(number)
    issue.setState("open")
    editIssue(issue, owner, repo, token)
  }

  def closeIssue(number: Integer, owner: String, repo: String, token: String): GitHubStatus = {

    logger.info(s"Invoking closeIssue with number '$number', owner '${owner}, repo '${repo} and token '${safeToken(token)}'");

    val issue = new EditIssue(number)
    issue.setState("closed")
    editIssue(issue, owner, repo, token)
  }

  def labelIssue(number: Integer, label: String, owner: String, repo: String, token: String): GitHubStatus = {

    logger.info(s"Invoking labelIssue with number '$number', label '${label}', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

    val issue = new EditIssue(number)
    issue.addLabel(label)
    editIssue(issue, owner, repo, token)
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

  def commentIssue(number: Integer, comment: String, owner: String, repo: String, token: String): GitHubStatus = {

    logger.info(s"Invoking labelIssue with number '$number', comment '${comment}', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

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

  def listIssues(days: Long = 1, token: String): java.util.List[GitHubIssue] = {

    logger.info(s"Invoking listIssues with days '$days' and token '${safeToken(token)}'");

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

    val result: Seq[GitHubIssue] = issues.filter(i => (i.pushedAt != null && i.pushedAt.isAfter(time)) || i.updatedAt.isAfter(time))
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
        GitHubIssue(id, title, url, issueUrl, repo, ts, i.state)
      })
      new JavaScriptArray[GitHubIssue](JavaConversions.seqAsJavaList(result))
  }

  def listIssues(search: String, owner: String, repo: String, token: String): java.util.List[GitHubIssue] = {

    logger.info(s"Invoking listIssues with search '$search', owner '$owner', repo '$repo' and token '${safeToken(token)}'");

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val li = new ListIssues
    li.setDirection("asc")
    li.setState("open")
    li.setSort("updated")

    val cri = SimpleCloudRepoId(owner, repo)
    var issues = gitHubServices.listIssues(cri, li).asScala

    val result: Seq[GitHubIssue] = issues.filter(i => i.pullRequest == null).sortWith((i1, i2) => i1.updatedAt.compareTo(i2.updatedAt) > 0)
        .filter(i => ( (search == null || search == "not-set") || (i.body.contains(search) || i.title.contains(search)))).toList.map(i => {
      val id = i.number
      val title = i.title
      // https://api.github.com/repos/octocat/Hello-World/issues/1347
      val url = i.url.replace("https://api.github.com/repos/", "https://github.com/").replace(s"/issues/${i.number}", "")
      // https://github.com/atomisthq/bot-service/issues/72
      val issueUrl = i.url.replace("https://api.github.com/repos/", "https://github.com/")
      // atomisthq/bot-service
      val repo = i.url.replace("https://api.github.com/repos/", "").replace(s"/issues/${i.number}", "")
      val ts = i.updatedAt.toEpochSecond
      GitHubIssue(id, title, url, issueUrl, repo, ts, i.state)
    }).slice(0, 10)
    new JavaScriptArray[GitHubIssue](JavaConversions.seqAsJavaList(result))
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

    logger.info(s"Invoking createRelease with tag '$tagName', owner '${owner}', repo '${repo}' and token '${safeToken(token)}'");

    val gitHubServices: GitHubServices = new GitHubServicesImpl(token)

    val repoId = new SimpleCloudRepoId(owner, repo)

    var tags: util.List[TagInfo] = null
    var tag: Option[String] = Option.apply(tagName)

    if (tagName == null || (tagName != null && tagName.length == 0)) {
      try {
        tags = gitHubServices.listTags(repoId)
      } catch {
        case e: ArtifactSourceAccessException =>
          return GitHubStatus(false, e.message)
        case ex: Exception => throw ex
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

  private def safeToken(token: String): String = {
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    }
    else {
      null
    }
  }
}

case class GitHubStatus(success: Boolean, message: String = "")

case class GitHubIssue(number: Int, title: String, url: String, issueUrl: String, repo: String, ts: Long, state: String)

