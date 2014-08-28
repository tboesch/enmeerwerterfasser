name := "EnMeer Werterfasser"

organization := "com.orbinista"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources() withJavadoc(),
  "org.rrd4j" % "rrd4j" % "2.2"
)

initialCommands := "import com.orbinista.enmeerwerterfasser._"

