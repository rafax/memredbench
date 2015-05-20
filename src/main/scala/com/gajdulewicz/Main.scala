package com.gajdulewicz

import java.util.UUID

import scala.collection.GenMap


/**
 * Created by gajduler on 18/05/15.
 */
object MemRedBench {
  val defaultScale = 1f
  val defaultMemcachedHost = ("localhost", 11211)
  val defaultRedisHost = ("localhost", 6379)

  def main(args: Array[String]): Unit = {
    val scale = args.headOption.map(_.toFloat).getOrElse(defaultScale)
    val (mcHost: String, mcPort: Int) = Option(System.getenv("MEMCACHED")).map(_.split(":") match {
      case Array(host, port, _*) => (host, port.toInt)
    }).getOrElse(defaultMemcachedHost)
    val (rHost: String, rPort: Int) = Option(System.getenv("REDIS")).map(_.split(":") match {
      case Array(host, port, _*) => (host, port.toInt)
    }).getOrElse(defaultRedisHost)
    println(s"Will run benchmark with scale $scale redis $rHost memcached $mcHost")
    val rp = new RedisPersistence(rHost, rPort)
    val mp = new MemcachedPersistence(mcHost, mcPort)
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
    var milis = runGetSet(data, rp)
    printStats(data.size, milis)
    println("Starting memcached")
    milis = runGetSet(data, mp)
    printStats(data.size, milis)
  }

  def getSetPar(scale: Float, rp: RedisPersistence, mp: MemcachedPersistence): Unit = {
    val data = (1 to (10000 * scale).toInt).map(n => UUID.randomUUID().toString -> UUID.randomUUID().toString).toMap.par
    println("Starting redis")
    var milis = runGetSet(data, rp)
    printStats(data.size, milis)
    println("Starting memcached")
    milis = runGetSet(data, mp)
    printStats(data.size, milis)
  }

  def printStats(length: Long, milis: Long): Unit = {
    println(s"Took $milis ms throughput ${length * 1000 / milis}/s avg ${milis.toFloat / length} ms")
  }

  def runGetSet(data: GenMap[String, String], r: Persistence[String]): Long = {
    time {
      data.map { d =>
        r.set(d._1, d._2)
      }
      data.map { kv =>
        val red = r.get(kv._1).get
        assert(red == kv._2)
      }
    }
  }

  def time[A](a: => A): Long = {
    val now = System.nanoTime
    a
    (System.nanoTime - now) / 1000000
  }

  def flushBefore[A](p: Persistence[_]*)(a: => A) = {
    p.foreach(_.flush())
    a
  }
}
