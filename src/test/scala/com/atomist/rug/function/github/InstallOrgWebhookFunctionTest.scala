package com.atomist.rug.function.github

import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Webhook
import com.atomist.util.JsonUtils

class InstallOrgWebhookFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  import TestConstants._

  it should "install organization webhook" in {
    val f = new InstallOrgWebHookFunction
    val response = f.invoke(testWebHookUrl, TestOrg, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
    ghs.deleteOrganizationWebhook(TestOrg, wh.id)
  }

  it should "fail to install duplicate organization webhook" in {
    val f = new InstallOrgWebHookFunction
    val webHookUrl = testWebHookUrl
    val response = f.invoke(webHookUrl, TestOrg, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
    val response2 = f.invoke(webHookUrl, TestOrg, ApiUrl, Token)
    response2.status shouldBe Status.Failure
    ghs.deleteOrganizationWebhook(TestOrg, wh.id)
  }
}
