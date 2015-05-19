name := "memredbench"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "2.7.2",

  "com.bionicspirit" %% "shade" % "1.6.0"
)

resolvers += "Spy" at "http://files.couchbase.com/maven2/"

