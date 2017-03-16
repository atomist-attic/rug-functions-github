package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonProperty

/**
  * Common for those functions that searche for issues.
  */
trait GitHubSearchIssues {

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
                   milestone: Milestone,
                   state: String,
                   @JsonProperty("pull_request") pullRequest: PullRequest,
                   @JsonProperty("pushed_at") pushedAt: OffsetDateTime,
                   @JsonProperty("created_at") createdAt: OffsetDateTime,
                   @JsonProperty("updated_at") updatedAt: OffsetDateTime,
                   @JsonProperty("closed_at") closedAt: OffsetDateTime,
                   assignees: Array[ResponseUser])

  case class ResponseUser(login: String,
                          id: Int,
                          url: String,
                          @JsonProperty("html_url") htmlUrl: String)

  case class IssueLabel(id: Integer, url: String, name: String, color: String, default: Boolean)

  case class Milestone(url: String, id: Integer, number: Integer)

  case class PullRequest(url: String)
}
