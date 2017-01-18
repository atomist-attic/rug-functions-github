package com.atomist.rug.commands.github

import java.util.Collections

import com.atomist.rug.kind.service.ServicesMutableView
import com.atomist.rug.spi.Command

class MessageBuilderCommands extends Command[ServicesMutableView] {

  override def name: String = "messageBuilder"

  override def nodeTypes: java.util.Set[String] = Collections.singleton("services")

  override def invokeOn(services: ServicesMutableView): Object = {
    services.serviceSource.messageBuilder
  }

}
