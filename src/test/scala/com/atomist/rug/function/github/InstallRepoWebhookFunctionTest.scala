package com.atomist.rug.function.github

import com.atomist.rug.function.github.TestConstants.{ApiUrl, Token}
import com.atomist.rug.spi.Handlers.Status
import com.atomist.source.git.domain.Webhook
import com.atomist.util.JsonUtils

class InstallRepoWebhookFunctionTest extends GitHubFunctionTest(Token, ApiUrl) {

  it should "install repo webhook" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val f = new InstallRepoWebHookFunction
    val response = f.invoke(testWebHookUrl, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
    ghs.deleteRepository(repo, owner)
  }

  it should "fail to install duplicate webhook" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val repo = tempRepo.name
    val owner = tempRepo.ownerName

    val f = new InstallRepoWebHookFunction
    val webHookUrl = testWebHookUrl
    val response = f.invoke(webHookUrl, repo, owner, ApiUrl, Token)
    response.status shouldBe Status.Success
    val body = response.body
    body shouldBe defined
    body.get.str shouldBe defined
    val wh = JsonUtils.fromJson[Webhook](body.get.str.get)
    wh.id should be > 0
    val response2 = f.invoke(webHookUrl, repo, owner, ApiUrl, Token)
    response2.status shouldBe Status.Failure
    ghs.deleteRepository(repo, owner)
  }

}
