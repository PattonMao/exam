package com.maojing.server

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.immutable.ListMap

import com.maojing.path.Path
import com.maojing.path.Path._

import akka.actor.Actor
import akka.actor.PoisonPill

class ServerActor() extends Actor {
  private val num: Int = 5
  @volatile
  var map: ListMap[String, Info] = ListMap()
  //月球车初始化计数器
  val startCount: AtomicInteger = new AtomicInteger(num)
  //月球车到达终点计数器，都达到终点后控制台退出
  val endCount: AtomicInteger = new AtomicInteger(num)

  //使用scheduler定时输出月球车的信息
  val scheduler = Executors.newSingleThreadScheduledExecutor()
  private def startPrint(): Unit = {
    scheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        map.foreach(ele => {
          val name = ele._1
          val string = predictAndPrintInfo(ele._2)
          println(name + " - " + string)
        })
      }
    }, 0, 500, TimeUnit.MILLISECONDS)
  }

  override def receive = {
    case state: State => map += (sender.path.name ->
      Info(state, System.currentTimeMillis()))
    case angle: Angle => {
      val now = System.currentTimeMillis()
      val name = sender.path.name
      if (!map.contains(name)) {
        throw new IllegalArgumentException("没有收到月球车的初始位置而是直接转向角度")
      }
      val oldInfo = map(name)
      map += (name -> calcInfo(oldInfo, angle, now))
    }
    case goal: Goal => {
      println(sender.path.name + ":" + goal)
      startCount.decrementAndGet
      //当所有月球报告了起止位置并开始行进时，启动控制台输出月球车的信息
      if (startCount.get == 0) {
        startPrint()
      }
    }
    case end: EndPoint => {
      //结束月球车
      sender ! PoisonPill.getInstance
      val name = sender.path.name
      map -= name
      println(name + " 到达终点")
      endCount.decrementAndGet()
      if (endCount.get == 0) {
        self ! PoisonPill.getInstance
        Thread.sleep(500)
        scheduler.shutdownNow()
      }
    }
  }

  /**
   * 根据时间差以及速度和方向，计算出月球车发送转向角度时的位置，也就是收到这条消息
   * 两秒前月球车的位置
   */
  private def calcInfo(oldInfo: Info, angle: Angle, now: Long): Info = {
    val oldState = oldInfo.state
    val newDirection = (oldState.direction + angle.value) % 360
    val newLocation = Path.calcLocation(oldState.location, oldState.speed, oldState.direction, now - oldInfo.times)
    Info(State(newLocation, newDirection, oldInfo.state.speed), now)
  }

  /**
   * 根据两秒延迟预测位置并返回字符串
   */
  private def predictAndPrintInfo(oldInfo: Info): String = {
    val time = System.currentTimeMillis() - oldInfo.times + 2 * 1000
    val oldState = oldInfo.state
    val newLocation = Path.calcLocation(oldState.location, oldState.speed, oldState.direction, time)
    "报告位置:" + oldState.location + " 预测位置:" + newLocation + " 方向:" + oldState.direction
  }

}

//控制中心每次需要打印的月球车信息,
private[server] class Info(val state: State, var times: Long)
object Info {
  def apply(state: State, times: Long): Info = new Info(state, times)
}