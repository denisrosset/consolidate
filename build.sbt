lazy val scalaVersions: Map[String, String] = Map("2.10" -> "2.10.6", "2.11" -> "2.11.8", "2.12" -> "2.12.1")
lazy val catsVersion = "0.9.0"
lazy val disciplineVersion = "0.7.2"
lazy val scalaCheckVersion = "1.13.4"
lazy val scalaTestVersion = "3.0.1"
lazy val shapelessVersion = "2.3.2"

name := "Consolidate"

organization := "com.faacets"

scalaVersion := scalaVersions("2.12")

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation", "-language:implicitConversions")

licenses := Seq("MIT" -> url("http://opensource.org/licenses/mit-license.php"))

homepage := Some(url("https://github.com/denisrosset/consolidate"))

bintrayRepository := "maven"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % catsVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.typelevel" %% "discipline" % disciplineVersion % "test"
)
