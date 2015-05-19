package com.gajdulewicz

import java.util.UUID

import scala.collection.GenMap


/**
 * Created by gajduler on 18/05/15.
 */
object MemRedBench {
  val defaultScale = 1f

  def main(args: Array[String]): Unit = {

    val scale = args.headOption.map(_.toFloat).getOrElse(defaultScale)
    println(s"Will run benchmark with scale $scale")
    val rp = new RedisPersistence("localhost", 6379)
    val mp = new MemcachedPersistence("localhost", 11211)
    println("Run sequential")
    flushBefore(rp, mp) {
      getSetSeq(scale, rp, mp)
    }
    println("Run parallel")
    flushBefore(rp, mp) {
      getSetPar(scale, rp, mp)
    }
  }


  def getSetSeq(scale: Float, rp: RedisPersistence, mp: MemcachedPersistence): Unit = {
    val data = (1 to (10000 * scale).toInt).map(n => UUID.randomUUID().toString -> UUID.randomUUID().toString).toMap
    println("Starting redis")
    runGetSet(data, rp)
    println("Starting memcached")
    runGetSet(data, mp)
  }

  def getSetPar(scale: Float, rp: RedisPersistence, mp: MemcachedPersistence): Unit = {
    val data = (1 to (10000 * scale).toInt).map(n => UUID.randomUUID().toString -> UUID.randomUUID().toString).toMap.par
    println("Starting redis")
    runGetSet(data, rp)
    println("Starting memcached")
    runGetSet(data, mp)
  }

  def runGetSet(data: GenMap[String, String], r: Persistence[String]): Unit = {
    time {
      data.map { d =>
        r.set(d._1, d._2)
      }
      data.map { kv =>
        val red = r.get(kv._1).get
      }
    }
  }

  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000000
    println("%d ms".format(micros))
    result
  }

  def flushBefore[A](p: Persistence[_]*)(a: => A) = {
    p.foreach(_.flush())
    a
  }
}
