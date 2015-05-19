name := "memredbench"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.0",

  "com.bionicspirit" %% "shade" % "1.6.0"
)

resolvers += "Spy" at "http://files.couchbase.com/maven2/"

