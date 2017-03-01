package com.atomist.rug.function.github

trait GitHubFunction {
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

case class GitHubIssue(number: Int, title: String, url: String, issueUrl: String, repo: String, ts: Long, state: String)

