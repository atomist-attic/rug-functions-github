package com.atomist.rug.function.github

import com.atomist.rug.runtime.RugSupport
import com.atomist.source.github.domain.ResponseUser

trait GitHubFunction
  extends RugSupport{
  /**
    * Sanitize a token
    * @param token
    * @return
    */
  def safeToken(token: String): String = {
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    }
    else {
      null
    }
  }
}

case class GitHubIssue(number: Int, title: String, url: String, issueUrl: String, repo: String, ts: Long, state: String, assignee: ResponseUser)

