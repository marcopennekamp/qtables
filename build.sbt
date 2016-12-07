val paradiseVersion = "2.1.0"

name := "qtables"
organization := "io.collap"
version := "0.1.0-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.10.5", "2.10.6", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5", "2.11.6", "2.11.7", "2.11.8")
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "io.getquill" %% "quill-jdbc" % "1.0.2-SNAPSHOT"
)

// Removes Scala version from artifacts
crossPaths := false

addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
