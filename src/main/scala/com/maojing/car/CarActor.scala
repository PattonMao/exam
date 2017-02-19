package com.maojing.car

import scala.collection.immutable.List
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.actorRef2Scala
import com.maojing.path.Path
import com.maojing.path.Location
import java.util.concurrent.Executors

/**
 * 模拟月球车，获取随机路径，然后每隔一秒报告当前位置以及方向速度。转向则立即报告。
 */
class CarActor() extends Actor {
  //控制中心actor
  val server = context.actorSelection("akka.tcp://Server@localhost:5101/user/serverActor")
  //获取随机路径
  val path: List[Path] = Path.getRandomPath(Location(0, 0), 15*60)
  val name = context.self.path.name

  import Path._

  //定时发送当前位置等信息
  Executors.newSingleThreadExecutor().submit(new Runnable {
    override def run: Unit = {
      //时间间隔报告信息，在转向时会改变
      var interval: Long = 1000
      require(path.last.isInstanceOf[EndPoint], "路线没有终点")
      var check:Boolean = false
      path.foreach({ p =>
        println(name + ":" + p)
        p match {
          case goal: Goal => {
            server ! goal
            Thread.sleep(2000)
          }
          //如果路径是当前的位置以及速度方向，则sleep 2秒模拟延迟
          case state: State => {
            Thread.sleep(interval)
            server ! state
          }
          //如果是转向则直接发送
          case angle: Angle => {
            Thread.sleep(angle.time)
            interval -= angle.time
            server ! angle
          }

          case end: EndPoint => {
            server ! end
            check  = true
          }
          
          case _ => server ! _
        }
      })
      if(!check) {
         println("???")
      }
    }

  })

  override def receive = {
    case e: Any => println(name + "recive:" + e)
  }
}

object CarActor {
  def apply(): CarActor = new CarActor()
}