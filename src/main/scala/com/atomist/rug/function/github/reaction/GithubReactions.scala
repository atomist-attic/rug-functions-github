package com.atomist.rug.function.github.reaction

import java.net.URL

object GithubReactions {

  case class Reaction(id: Int, url: URL, content: String)

}
