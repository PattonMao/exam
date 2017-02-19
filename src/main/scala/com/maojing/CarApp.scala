package com.maojing

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import com.maojing.car.CarActor
import com.maojing.path.Path
import com.maojing.path.Location

object CarApp extends App {
  val system = ActorSystem("Car", ConfigFactory.load().getConfig("Car"))
  val car1 = system.actorOf(Props[CarActor], "car1")
  val car2 = system.actorOf(Props[CarActor], "car2")
  val car3 = system.actorOf(Props[CarActor], "car3")
  val car4 = system.actorOf(Props[CarActor], "car4")
  val car5 = system.actorOf(Props[CarActor], "car5")
}