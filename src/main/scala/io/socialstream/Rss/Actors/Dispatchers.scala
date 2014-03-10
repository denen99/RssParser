package io.socialstream.Rss.Actors

import akka.actor.{ActorSystem, Props}
import io.socialstream.Rss.{URLParser, URLFetcher, RSSUpdater}
import akka.routing.RoundRobinRouter

object Dispatchers  {

  def startActorSystem = ActorSystem("RssParser")

  val system = startActorSystem

  val urlFetcher = system.actorOf(Props[URLFetcher].withRouter(RoundRobinRouter(nrOfInstances=5)).withDispatcher("fetcher-balance-dispatcher"), "urlFetcher")
  val urlParser = system.actorOf(Props[URLParser].withRouter(RoundRobinRouter(nrOfInstances=5)).withDispatcher("parser-balance-dispatcher"), "urlParser")
  val rssUpdater =  system.actorOf(Props[RSSUpdater].withRouter(RoundRobinRouter(nrOfInstances=5)).withDispatcher("parser-balance-dispatcher"), "rssUpdater")

}
