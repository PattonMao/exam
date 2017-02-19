package com.maojing

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import com.maojing.server.ServerActor

object ServerApp extends App {
  val system = ActorSystem("Server", ConfigFactory.load().getConfig("Server"))
  val controlServer = system.actorOf(Props[ServerActor], "serverActor")
}