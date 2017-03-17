package com.atomist.rug.function.github

import com.atomist.rug.runtime.RugSupport

trait GitHubFunction
  extends RugSupport {

  /**
    * Sanitize a token.
    */
  def safeToken(token: String): String = {
    if (token != null) {
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    } else {
      null
    }
  }
}

object GitHubFunction {

  val ApiUrl = "https://api.github.com"
}