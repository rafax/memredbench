package com.gajdulewicz

import java.io.PrintStream
import java.net.Socket

import com.redis.{RedisClientPool, Seconds}
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

  val clientPool = new RedisClientPool(address, port)

  override def set(k: String, v: String): Unit = clientPool.withClient { c => c.set(k, v, false, Seconds(defaultExpiry.toSeconds)) }

  override def get(k: String): Option[String] = clientPool.withClient { c => c.get(k) }

  def append(listKey: String, v: String): Unit = clientPool.withClient { c => c.lpush(listKey, v) }

  def getAll(listKey: String): Seq[String] = clientPool.withClient { c => c.lrange(listKey, 0, -1).getOrElse(Nil).flatten }

  override def flush(): Unit = clientPool.withClient { c => c.flushall }

}

class RedisPipelinedPersistence(address: String, port: Int) extends Persistence[String] {

val clientPool = new RedisClientPool(address, port)

override def set(k: String, v: String): Unit = clientPool.withClient { c => c.set(k, v, false, Seconds(defaultExpiry.toSeconds)) }

override def get(k: String): Option[String] = clientPool.withClient { c => c.get(k) }

def append(listKey: String, v: String): Unit = clientPool.withClient { c => c.lpush(listKey, v) }

def getAll(listKey: String): Seq[String] = clientPool.withClient { c => c.lrange(listKey, 0, -1).getOrElse(Nil).flatten }

override def flush(): Unit = clientPool.withClient { c => c.flushall }

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