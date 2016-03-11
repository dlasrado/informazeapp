name := """infomaze"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "com.restfb" % "restfb" % "1.19.0",
  "org.json" % "json" % "20070829",
  "org.mongodb" % "mongo-java-driver" % "3.0.4"
)


