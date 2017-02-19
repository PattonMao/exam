package com.maojing.path
/**
 * 表示坐标位置，x,y坐标的值
 */
class Location(val x: Double, val y: Double) extends Serializable {
  override def equals(other: Any) = {
    other match {
      case Location => x == other.asInstanceOf[Location].x && y == other.asInstanceOf[Location].y
      case _        => false
    }
  }
  override def toString: String = "[" + x + "," + y + "]"
}

object Location {
  def apply(x: Double, y: Double): Location = new Location(x, y)
}