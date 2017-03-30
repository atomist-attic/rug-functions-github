package com.atomist.rug.function.github

import com.atomist.source.github.GitHubServices.{PullRequestBranch, PullRequestStatus}
import org.kohsuke.github.GHPullRequest

object GitHubPullRequests {

  import GitHubFunction._

//  def mapPullRequest(pr: GHPullRequest): PullRequestStatus =
//    PullRequestStatus(pr.getId, pr.getUrl.toExternalForm, pr.getHtmlUrl.toExternalForm, pr.getNumber, pr.getTitle,
//      pr.getBody, convertDate(pr.getCreatedAt), convertDate(pr.getUpdatedAt), convertDate(pr.getMergedAt),
//      convertDate(pr.getClosedAt), PullRequestBranch(pr.getHead.getRef, pr.getHead.getSha),
//      PullRequestBranch(pr.getBase.getRef, pr.getBase.getSha), pr.getState.name(), pr.isMerged, pr.getMergeable,
//      pr.getMergeableState, pr.getCommentsCount, pr.getReviewComments)
}
