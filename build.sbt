import sbtassembly.Plugin.AssemblyKeys._

assemblySettings

jarName in assembly := { s"${name.value}-${version.value}.jar" }

artifact := Artifact("smallibs.rapido")

name := "rapido"

version := "0.1-alpha"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-deprecation", "-feature")

mainClass := Some("smallibs.rapido.Rapido")

// Test corner

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.specs2" %% "specs2" % "1.12.3" % "test"
)

fork in Test := true

