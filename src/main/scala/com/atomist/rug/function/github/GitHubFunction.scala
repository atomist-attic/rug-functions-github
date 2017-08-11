package com.atomist.rug.function.github

import java.time.{OffsetDateTime, ZoneId}
import java.util.Date

import com.atomist.rug.runtime.Rug

trait GitHubFunction extends Rug {

  /**
    * Sanitize a token.
    */
  def safeToken(token: String): String =
    if (token != null)
      token.charAt(0) + ("*" * (token.length() - 2)) + token.last
    else
      null
}

object GitHubFunction {

  val ApiUrl = "https://api.github.com"

  val Events = Seq("*")

  def convertDate(date: Date): OffsetDateTime =
    if (date == null) null else OffsetDateTime.ofInstant(date.toInstant, ZoneId.systemDefault())
}
