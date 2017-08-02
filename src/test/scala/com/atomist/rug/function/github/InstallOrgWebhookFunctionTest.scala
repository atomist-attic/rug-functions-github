package com.atomist.rug.function.github

import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Webhook
import com.atomist.util.JsonUtils

class InstallOrgWebhookFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  ignore should "install organization webhook" in {
    val f = new InstallOrgWebHookFunction
    val response = f.invoke("http://example.com/webhook", "atomisthqtest", ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    println(wh.id)
  }
}
