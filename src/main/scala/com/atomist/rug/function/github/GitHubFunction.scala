package com.atomist.rug.function.github

import com.atomist.rug.runtime.RugSupport
import com.atomist.source.github.util.HttpMethods.Get
import com.atomist.source.github.util.RestGateway.httpRequest
import com.fasterxml.jackson.annotation.JsonProperty

import scala.reflect.Manifest

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

  /**
    * Paginates a search and returns an aggregated list of results.
    */
  def paginateResults[T](token: String,
                         firstPage: Seq[T],
                         url: String,
                         queryString: Map[String, AnyRef] = Map.empty)
                        (implicit m: Manifest[T]): Seq[T] = {
    def nextPage(token: String, url: String, accumulator: Seq[T]): Seq[T] = {
      val response = httpRequest[Seq[T]](token, url, Get, None, queryString)
      val pages = accumulator ++ response.obj
      response.linkHeader.get("next").map(nextPage(token, _, pages)).getOrElse(pages)
    }

    nextPage(token, url, firstPage)
  }
}

case class GitHubIssue(number: Int,
                       title: String,
                       url: String,
                       issueUrl: String,
                       repo: String,
                       ts: Long,
                       state: String,
                       assignee: ResponseUser)

case class ResponseUser(login: String,
                        id: Int,
                        url: String,
                        @JsonProperty("html_url") htmlUrl: String)