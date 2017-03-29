package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonProperty

object GitHubPullRequests {

  case class PullRequestStatus(var id: Long,
                               var url: String,
                               @JsonProperty("html_url") var htmlUrl: String,
                               var number: Int,
                               var title: String,
                               var body: String,
                               @JsonProperty("created_at") var createdAt: OffsetDateTime,
                               @JsonProperty("updated_at") var updatedAt: OffsetDateTime,
                               @JsonProperty("merged_at") var mergedAt: OffsetDateTime,
                               @JsonProperty("closed_at") var closedAt: OffsetDateTime,
                               var head: PullRequestBranch,
                               var base: PullRequestBranch,
                               var state: String)

  case class PullRequestBranch(ref: String, sha: String)

}
