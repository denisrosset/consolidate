name := "Consolidate"

organization := "com.faacets"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")

initialCommands in console := """import com.faacets.consolidate._; import algebra._; import syntax.all._"""

libraryDependencies ++= Seq(
  "org.typelevel" %% "machinist" % "0.3.0"
)
