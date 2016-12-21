val paradiseVersion = "2.1.0"

name := "qtables"
organization := "io.collap"
version := "0.1.2-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.bintrayIvyRepo("scalameta", "maven")

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.10.5", "2.10.6", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5", "2.11.6", "2.11.7", "2.11.8")
libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "1.4.0",
  "io.getquill" %% "quill-jdbc" % "1.0.2-SNAPSHOT"
)

scalacOptions += "-Xplugin-require:macroparadise"
// temporary workaround for https://github.com/scalameta/paradise/issues/10
scalacOptions in (Compile, console) := Seq() // macroparadise plugin doesn't work in repl yet.
// temporary workaround for https://github.com/scalameta/paradise/issues/55
sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.

// Removes Scala version from artifacts
crossPaths := false

// A dependency on macro paradise 3.x is required to both write and expand
// new-style macros.  This is similar to how it works for old-style macro
// annotations and a dependency on macro paradise 2.x.
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-beta4" cross CrossVersion.full)
