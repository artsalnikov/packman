ThisBuild / organization := "ru.sberbank.xops.packman"
ThisBuild / version := "0.1-SNAPSHOT"

scalaVersion := "2.12.14"

// disable using the Scala version in output paths and artifacts
crossPaths := false

lazy val root = (project in file("."))
  .aggregate(packmanPlugin, packman)

lazy val packmanPlugin = (project in file("sbt-packman"))
  .enablePlugins(SbtPlugin)
  .dependsOn(packman)
  .settings(
    name := "sbt-packman",
    libraryDependencies ++= Seq(
      "org.reflections"   % "reflections"   % "0.9.12",
      "com.google.guava"  % "guava"         % "30.1.1-jre"  % "compile"
    )
  )

lazy val packman = (project in file("packman"))
  .settings(
    name := "packman",
    crossPaths := false,
    libraryDependencies ++= Seq(
      "org.reflections"   % "reflections"   % "0.9.12",
      "com.google.guava"  % "guava"         % "30.1.1-jre"  % "compile"
    )
  )