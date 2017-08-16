package com.atomist.rug.function.github

import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Webhook
import com.atomist.util.JsonUtils

class InstallOrgWebhookFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  "InstallOrgWebhookFunctionTest" should "install organization webhook" in {
    val org = "atomisthqtest"
    val url = "http://example.com/webhook"
    ghs.listOrganizationWebhooks(org)
      .find(_.config.url == url)
      .foreach(wh => ghs.deleteOrganizationWebhook(org, wh.id))

    val f = new InstallOrgWebHookFunction
    val response = f.invoke(url, org, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
  }

  it should "fail to install duplicate organization webhook" in {
    val org = "atomisthqtest"
    val url = "http://example.com/webhook"
    ghs.listOrganizationWebhooks(org)
      .find(_.config.url == url)
      .foreach(wh => ghs.deleteOrganizationWebhook(org, wh.id))

    val f = new InstallOrgWebHookFunction
    val response = f.invoke(url, org, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
    val response2 = f.invoke(url, org, ApiUrl, Token)
    response2.status shouldBe Status.Failure
  }
}
