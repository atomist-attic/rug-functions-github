package com.atomist.rug.spi

import com.atomist.param.{SimpleParameterValue, SimpleParameterValues}
import com.atomist.rug.spi.Handlers.{Response, Status}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}

class AnnotatedRugFunctionTest extends FlatSpec with Matchers with LazyLogging {

  class MyFunction extends AnnotatedRugFunction {

    @com.atomist.rug.spi.annotation.RugFunction(
      name = "my-function",
      description = "This is a simple test function",
      tags = Array( new com.atomist.rug.spi.annotation.Tag(name = "test")))
    def run(@com.atomist.rug.spi.annotation.Parameter(name = "param1", pattern = "@any") param1: String,
            @com.atomist.rug.spi.annotation.Secret(name = "secret1", path = "path1") secret1: String): Response = {
      Response(Status.Success, Option(s"This is ${param1} and ${secret1}"))
    }
  }

  it should "find all metadata" in {
    val myFunction = new MyFunction
    myFunction.name should be("my-function")
    myFunction.description should be("This is a simple test function")
    myFunction.tags.size should be(1)
    myFunction.parameters.size should be(1)
    myFunction.secrets.size should be(1)
  }

  it should "invoke function method" in {
    val myFunction = new MyFunction
    val response = myFunction.run(SimpleParameterValues(Seq(SimpleParameterValue("param1", "value1"), SimpleParameterValue("secret1", "value2"))))
    response.msg.get should be("This is value1 and value2")
  }

}
