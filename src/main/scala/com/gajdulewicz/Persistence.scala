package com.gajdulewicz

import java.io.PrintStream
import java.net.Socket

import redis.clients.jedis.{Jedis, JedisPool}
import shade.memcached.{Configuration, Memcached}

import scala.concurrent.duration.Duration
import scala.io.BufferedSource

/**
 * Created by gajduler on 18/05/15.
 */
trait Persistence[T] {

  val defaultExpiry = Duration("30 seconds")

  def set(k: String, v: T)

  def get(k: String): Option[T]

  def flush(): Unit

  // delete, delete all
}

class RedisPersistence(address: String, port: Int) extends Persistence[String] {

  val clientPool = new JedisPool(address, port)

  def withClient[T](body: Jedis => T) = {
    val client = clientPool.getResource
    try {
      body(client)
    } finally {
      client.close()
    }
  }

  override def set(k: String, v: String): Unit = withClient { c => c.setex(k, defaultExpiry.toSeconds.toInt, v) }

  override def get(k: String): Option[String] = withClient { c => Option(c.get(k)) }

  override def flush(): Unit = withClient { c => c.flushAll() }
}


class MemcachedPersistence(address: String, port: Int) extends Persistence[String] {

  import scala.concurrent.ExecutionContext.Implicits.{global => ec}

  val client = Memcached(Configuration(s"$address:$port"), ec)

  override def set(k: String, v: String): Unit = client.awaitSet(k, v, defaultExpiry)

  override def get(k: String): Option[String] = client.awaitGet[String](k)

  override def flush(): Unit = {
    val s = new Socket(address, port)
    lazy val in = new BufferedSource(s.getInputStream()).getLines()
    val out = new PrintStream(s.getOutputStream(), true)
    out.println("flush_all")
    in.next()
  }
}