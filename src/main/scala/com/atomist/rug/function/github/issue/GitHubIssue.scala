package com.atomist.rug.function.github.issue

import com.atomist.source.git.domain.User

case class GitHubIssue(number: Int,
                       title: String,
                       url: String,
                       issueUrl: String,
                       repo: String,
                       ts: Long,
                       state: String,
                       assignee: User)
