name := "EnMeer Werterfasser"

organization := "com.orbinista"

version := "0.0.1"

scalaVersion := "2.10.3"

mainClass in Compile := Some("com.orbinista.enmeerwerterfasser.OpenceanDemo")

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources() withJavadoc(),
  "com.neuronrobotics" % "nrjavaserial" % "3.7.5.1",
  "org.slf4j" % "slf4j-jdk14" % "1.7.7",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7",
  "com.typesafe.akka" %% "akka-actor" % "2.3.5",
  "com.github.nscala-time" %% "nscala-time" % "1.4.0",
  "com.github.tototoshi" %% "scala-csv" % "1.0.0"
)

initialCommands := "import com.orbinista.enmeerwerterfasser._"

