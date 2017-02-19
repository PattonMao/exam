package com.maojing.path

import scala.util.Random

class Path {

}

object Path {
  /**
   * 表示月球车当前状态的所有信息，位置，速度（每秒移动多少坐标值），方向（按顺时针计算的与x轴的夹角）
   */
  case class State(val location: Location, val speed: Double, val direction: Double) extends Path
  /**
   * 表示月球车转向角度,time用来表示距离上次发送状态之后多少时间进行转向，生成路径时使用
   */
  case class Angle(val value: Double, @transient val time: Long) extends Path
  /**
   * 起始位置和目标，月球车移动之前需要报告的信息
   */
  case class Goal(val start: Location, val end: Location, val speed: Double) extends Path {
    override def toString: String = "start:" + start + " end:" + end
  }

  /**
   * 到达终点
   */
  case class EndPoint(val end: Location) extends Path

  /**
   * 根据起始坐标和最少使用时间，生成一条随机线路(坐标可以为负)。
   */
  def getRandomPath(start: Location, interval: Int): List[Path] = {
    var list: List[Path] = List()
    val initialSpeed: Double = Random.nextInt(10) + 1
    var preLocation = start
    var preSpeed: Double = initialSpeed
    var preDirection: Double = Random.nextInt(90) + 1 + { if (Random.nextBoolean) 0 else 270 }
    var preTime = 1000

    var count = interval

    while (count > 0) {
      preLocation = calcLocation(preLocation, preSpeed, preDirection, preTime)
      val speed: Double = Random.nextInt(50) + 30
      list = list :+ State(preLocation, speed, preDirection)

      //给定1/8的概率遇到障碍转向，走到最后一步即为终点不再转向
      if (Random.nextInt(8) == 0 && count > 1) {
        //假设不存在以返回的角度往回走，既是保证转向角度(0,90]U[270,360)
        val angle: Double = Random.nextInt(90) + 1 + { if (Random.nextBoolean) 0 else 270 }
        preDirection = (preDirection + angle) % 360
        //在两次定是报告之间的某个时刻转向，假定每秒报告位置时不会转向
        val time = Random.nextInt(999) + 1
        preTime = 1000 - time
        preLocation = calcLocation(preLocation, preSpeed, preDirection, time)
        list = list :+ Angle(angle, time)
      }
      count -= 1
    }
    val last = list.last
    list = Goal(start, last.asInstanceOf[State].location, initialSpeed) :: list
    list = list.dropRight(1)
    val end = EndPoint(last.asInstanceOf[State].location)
    list :+ end
  }

  /**
   * 根据前一坐标和速度方向，计算出经过time毫秒之后的坐标
   */
  def calcLocation(preLocation: Location, speed: Double, direction: Double, time: Long): Location = {
    val distance = time * speed / 1000
    val x = distance * Math.cos(direction) + preLocation.x
    val y = distance * Math.sin(direction) + preLocation.y
    Location(x, y)
  }

}