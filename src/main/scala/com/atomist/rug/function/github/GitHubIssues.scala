package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonProperty
import org.kohsuke.github.GHIssue

import scala.collection.JavaConverters._

object GitHubIssues {

  import GitHubFunction._

  case class GitHubIssue(number: Int,
                         title: String,
                         url: String,
                         issueUrl: String,
                         repo: String,
                         ts: Long,
                         state: String,
                         assignee: ResponseUser)

  case class Issue(number: Int,
                   id: Int,
                   title: String,
                   url: String,
                   body: String,
                   user: ResponseUser,
                   assignee: ResponseUser,
                   labels: Array[IssueLabel],
                   milestone: Option[Milestone],
                   state: String,
                   @JsonProperty("pull_request") pullRequest: Option[PullRequest],
                   repository: Option[IssueRepository],
                   @JsonProperty("created_at") createdAt: OffsetDateTime,
                   @JsonProperty("updated_at") updatedAt: OffsetDateTime,
                   @JsonProperty("closed_at") closedAt: Option[OffsetDateTime],
                   assignees: Option[Array[ResponseUser]])

  case class ResponseUser(login: String,
                          id: Int,
                          url: String,
                          @JsonProperty("html_url") htmlUrl: String)

  case class IssueLabel(url: String, name: String, color: String)

  case class Milestone(url: String, id: Integer, number: Integer)

  case class PullRequest(url: String)

  case class IssueRepository(@JsonProperty("pushed_at") pushedAt: OffsetDateTime)

  def mapIssue(gHIssue: GHIssue): Issue = {
    val gHUser = gHIssue.getUser
    val user = ResponseUser(gHUser.getLogin, gHUser.getId, gHUser.getUrl.toExternalForm, gHUser.getHtmlUrl.toExternalForm)
    val gHAssignee = gHIssue.getAssignee
    val assignee = ResponseUser(gHAssignee.getLogin, gHAssignee.getId, gHAssignee.getUrl.toExternalForm, gHAssignee.getHtmlUrl.toExternalForm)
    val labels = gHIssue.getLabels.asScala.map(l => IssueLabel(l.getUrl, l.getName, l.getColor)).toArray
    val gHMilestone = gHIssue.getMilestone
    val milestone = if (gHMilestone == null) None else Some(Milestone(gHMilestone.getUrl.toExternalForm, gHMilestone.getId, gHMilestone.getNumber))
    val pullRequest = if (gHIssue.getPullRequest == null) None else Some(PullRequest(gHIssue.getPullRequest.getUrl.toExternalForm))
    val repository = if (gHIssue.getRepository == null) None else Some(IssueRepository(convertDate(gHIssue.getRepository.getPushedAt)))
    val closedAt = if (gHIssue.getClosedAt == null) None else Some(convertDate(gHIssue.getClosedAt))
    val assignees = if (gHIssue.getAssignees.isEmpty) None
    else Some(gHIssue.getAssignees.asScala.map(a => ResponseUser(a.getLogin, a.getId, a.getUrl.toExternalForm, a.getHtmlUrl.toExternalForm)).toArray)

    Issue(gHIssue.getNumber, gHIssue.getId, gHIssue.getTitle, gHIssue.getUrl.toExternalForm, gHIssue.getBody,
      user, assignee, labels, milestone, gHIssue.getState.name, pullRequest, repository, convertDate(gHIssue.getCreatedAt),
      convertDate(gHIssue.getUpdatedAt), closedAt, assignees)
  }
}

