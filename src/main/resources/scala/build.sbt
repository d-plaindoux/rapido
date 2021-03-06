name := "@USE::project"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.sun.jersey" % "jersey-core" % "1.8",
  "com.sun.jersey" % "jersey-client" % "1.8",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.specs2" %% "specs2" % "1.12.3" % "test"
)

fork in Test := true
