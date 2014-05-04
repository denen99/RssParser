import AssemblyKeys._ // put this at the top of the file

assemblySettings

version := "0.0.3"

//fork := true

scalaVersion := "2.10.2"

resolvers += "rediscala" at "https://github.com/etaty/rediscala-mvn/raw/master/releases/"

resolvers += "OpenNLP Repository" at "http://opennlp.sourceforge.net/maven2/"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"

name := "RssParser"

libraryDependencies ++= Seq(
   "net.debasishg" %% "redisreact" % "0.3",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
   "com.typesafe.akka" %% "akka-actor" % "2.2.3",
   "org.slf4j" % "slf4j-api" % "1.7.5",
   "ch.qos.logback" % "logback-classic" % "1.0.6",
   "org.scala-lang.modules" %% "scala-async" % "0.9.0-M4",
    "com.google.code.gson" % "gson" % "2.2.4",
    "org.apache.opennlp" % "opennlp-tools" % "1.5.2-incubating",
    "net.codingwell" %% "scala-guice" % "3.0.2",
    "org.json4s" %% "json4s-native" % "3.2.4",
    "com.foursquare" %% "rogue-field" % "2.2.0" intransitive(),
    "com.foursquare" %% "rogue-core" % "2.2.0" intransitive(),
    "com.foursquare" %% "rogue-lift" % "2.2.0" intransitive(),
    "com.foursquare" %% "rogue-index" % "2.2.0" intransitive(),
    "net.liftweb" %% "lift-mongodb-record" % "2.5.1",
 //   "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.google.guava" % "guava" % "15.0",
    "com.google.code.findbugs" % "jsr305" % "2.0.2",
    "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test" ,
    "org.jsoup" % "jsoup" % "1.7.3"
)

//runUberJar := {
//  val uberJar = createUberJar.value
//  val options = ForkOptions()
//  val arguments = Seq("-cp", uberJar.getAbsolutePath, "global.Global")
//  Fork.java(options, arguments)
//} 
