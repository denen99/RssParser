version := "0.0.3"

//fork := true

fork in run := true


scalaVersion := "2.10.2"

resolvers += "rediscala" at "https://github.com/etaty/rediscala-mvn/raw/master/releases/"

resolvers += "OpenNLP Repository" at "http://opennlp.sourceforge.net/maven2/"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"

name := "RssParser"


javaOptions in run += "-Xmx2G -Xms2G"

def unpack(target: File, f: File, log: Logger) = {
  log.debug("unpacking " + f.getName)
  if (!f.isDirectory) sbt.IO.unzip(f, target, unpackFilter(target))
}

def create(depDir: File, binDir: File, buildJar: File) = {
  def files(dir: File) = {
    val fs = (dir ** "*").get.filter(d => d != dir)
    fs.map(x => (x, x.relativeTo(dir).get.getPath))
  }
  sbt.IO.zip(files(binDir) ++ files(depDir), buildJar)
}

val dependentJarDirectory = settingKey[File]("location of the unpacked dependent jars")

dependentJarDirectory := target.value / "dependent-jars"

val createDependentJarDirectory = taskKey[File]("create the dependent-jars directory")

createDependentJarDirectory := {
  sbt.IO.createDirectory(dependentJarDirectory.value)
  dependentJarDirectory.value
}

val excludes = List("meta-inf", "license", "play.plugins", "reference.conf")

def unpackFilter(target: File) = new NameFilter {
  def accept(name: String) = {
    !excludes.exists(x => name.toLowerCase().startsWith(x)) &&
      !file(target.getAbsolutePath + "/" + name).exists
  }
}

val unpackJars = taskKey[Seq[_]]("unpacks a dependent jars into target/dependent-jars")

unpackJars := {
  val dir = createDependentJarDirectory.value
  val log = streams.value.log
  Build.data((dependencyClasspath in Runtime).value).map ( f => unpack(dir, f, log))
}

val createUberJar = taskKey[File]("create jar which we will run")

createUberJar := {
  val ignored = unpackJars.value
  create (dependentJarDirectory.value, (classDirectory in Compile).value, target.value / "build.jar");
  target.value / "build.jar"
}

val runRss = taskKey[Process]("Run RSSParser")

runRss := {
   val jar = createUberJar.value
   val options = ForkOptions()
   val args = Seq("-cp", jar.getAbsolutePath,"Rss.Main")
   Fork.java.fork(options,args)
}

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
