package com.atomist.rug.function.github

import org.kohsuke.github.GHHook

import scala.collection.JavaConverters._

object GitHubWebHooks {

  case class WebhookInfo(name: String, url: String, contentType: String = "json", events: Seq[String], id: Int)

  def mapHook(gHHook: GHHook): WebhookInfo =
    WebhookInfo(gHHook.getName, gHHook.getUrl.toExternalForm, gHHook.getConfig.get("content_type"),
      gHHook.getEvents.asScala.toSeq.map(_.name()), gHHook.getId)
}
