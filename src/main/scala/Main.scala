import akka.actor.ActorSystem
import slack.api.BlockingSlackApiClient
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.Scalaz._

object Main {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("slack")

    val channelPairs = Seq(
      ("bot-test", "bot-test")
    )

    val leftTeamToken = "同期したいslack groupのAPIトークン1"
    val rtmClient1 = SlackRtmClient(leftTeamToken)
    val blockingClient1 = BlockingSlackApiClient(leftTeamToken)
    val _1to2 = Map(channelPairs: _*)

    val rightTeamToken = "同期したいslack groupのAPIトークン1"
    val rtmClient2 = SlackRtmClient(rightTeamToken)
    val blockingClient2 = BlockingSlackApiClient(rightTeamToken)
    val _2to1 = _1to2.map(_.swap)

    rtmClient2.onMessage { message =>
      val channel = blockingClient2.getChannelInfo(message.channel)
      println(s"Channel: ${channel.name}, User: ${message.user}, Message: ${message.text}")
    }

    def registerRelay(
      fromRtmClient: SlackRtmClient,
      fromBlockingClient: BlockingSlackApiClient,
      fromToChannelNameMap: Map[String, String],
      toRtmClient: SlackRtmClient,
      toBlockingClient: BlockingSlackApiClient) =
      fromRtmClient.onMessage { message =>
        val sendUser = fromBlockingClient.getUserInfo(message.user)
        val channel = fromBlockingClient.getChannelInfo(message.channel)
        fromToChannelNameMap.get(channel.name).map { channelName =>
          toBlockingClient.postChatMessage(
            "#" + channelName,
            // nameでも指定できるからそうしているが、
            // 本当はchannels.listからIDで指定したほうがいいと思う
            // 参考: https://api.slack.com/methods/chat.postMessage
            message.text,
            sendUser.name.some,
            false.some,
            iconUrl = sendUser.profile.get.image_48.some
          )
        }
      }

    registerRelay(rtmClient1, blockingClient1, _1to2, rtmClient2, blockingClient2)
    registerRelay(rtmClient2, blockingClient2, _2to1, rtmClient1, blockingClient1)
  }
}
