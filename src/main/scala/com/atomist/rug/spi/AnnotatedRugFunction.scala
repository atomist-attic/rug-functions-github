package com.atomist.rug.spi

import java.lang.reflect.Method

import com.atomist.param.{Parameter, ParameterValues, Tag}
import com.atomist.rug.spi.Handlers.Response
import org.springframework.core.annotation.AnnotationUtils

trait AnnotatedRugFunction extends RugFunction {

  override def secrets: Seq[Secret] = {
    functionMethod().getParameters.map(p => {
      AnnotationUtils.findAnnotation(p, classOf[com.atomist.rug.spi.annotation.Secret]) match {
        case s: com.atomist.rug.spi.annotation.Secret => Option(Secret(s.name(), s.path()))
        case _ => Option.empty
      }
    }).flatMap(x => x).toSeq
  }

  override def name: String = functionAnnotation().name()

  override def description: String = functionAnnotation().description()

  override def tags: Seq[Tag] = functionAnnotation().tags().map(t => Tag(t.name(), t.name())).toSeq

  private def functionAnnotation(): com.atomist.rug.spi.annotation.RugFunction =
    functionMethod().getAnnotation(classOf[com.atomist.rug.spi.annotation.RugFunction])

  private def functionMethod(): Method = {
    getClass.getMethods.filter(m => m.getAnnotation(classOf[com.atomist.rug.spi.annotation.RugFunction]) != null).headOption match {
      case Some(m) => m
      case _ => throw new IllegalArgumentException(s"${getClass.getName} has no method annotated with @RugFunction")
    }
  }

  override def parameters: Seq[Parameter] = {
    functionMethod().getParameters.map(p => {
      AnnotationUtils.findAnnotation(p, classOf[com.atomist.rug.spi.annotation.Parameter]) match {
        case pa: com.atomist.rug.spi.annotation.Parameter => Option(Parameter(pa.name(), pa.pattern()))
        case _ => Option.empty
      }
    })
  }.flatMap(x => x).toSeq

  override def run(parameters: ParameterValues): Response = {
    val method = functionMethod
    val args = method.getParameters.map(p => {
      val parameterAnnotation = p.getAnnotation(classOf[com.atomist.rug.spi.annotation.Parameter])
      val secretAnnotation = p.getAnnotation(classOf[com.atomist.rug.spi.annotation.Secret])
      if (parameterAnnotation == null && secretAnnotation != null) {
        parameters.stringParamValue(secretAnnotation.name())
      }
      else if (parameterAnnotation != null && secretAnnotation == null) {
        parameters.stringParamValue(parameterAnnotation.name())
      }
      else {
        throw new IllegalArgumentException(s"Parameter ${p.getName} not annotated with either @Secret or @Parameter")
      }
    })
    method.invoke(this, args:_*).asInstanceOf[Response]
  }
}
