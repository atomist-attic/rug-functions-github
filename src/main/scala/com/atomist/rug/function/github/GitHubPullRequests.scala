package com.atomist.rug.function.github

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonProperty
import org.kohsuke.github.GHPullRequest

object GitHubPullRequests {

  import GitHubFunction._

  case class PullRequest(id: Long,
                         url: String,
                         @JsonProperty("html_url") var htmlUrl: String,
                         number: Int,
                         title: String,
                         body: String,
                         @JsonProperty("created_at") createdAt: OffsetDateTime,
                         @JsonProperty("updated_at") updatedAt: OffsetDateTime,
                         @JsonProperty("merged_at") mergedAt: OffsetDateTime,
                         @JsonProperty("closed_at") closedAt: OffsetDateTime,
                         head: PullRequestBranch,
                         base: PullRequestBranch,
                         state: String,
                         merged: Boolean,
                         mergeable: Boolean,
                         @JsonProperty("mergeable_state") mergeableState: String,
                         comments: Int,
                         @JsonProperty("review_comments") reviewComments: Int)

  case class PullRequestBranch(ref: String, sha: String)

  def mapPullRequest(pr: GHPullRequest): PullRequest =
    PullRequest(pr.getId, pr.getUrl.toExternalForm, pr.getHtmlUrl.toExternalForm, pr.getNumber, pr.getTitle,
      pr.getBody, convertDate(pr.getCreatedAt), convertDate(pr.getUpdatedAt), convertDate(pr.getMergedAt),
      convertDate(pr.getClosedAt), PullRequestBranch(pr.getHead.getRef, pr.getHead.getSha),
      PullRequestBranch(pr.getBase.getRef, pr.getBase.getSha), pr.getState.name(), pr.isMerged, pr.getMergeable,
      pr.getMergeableState, pr.getCommentsCount, pr.getReviewComments)
}
