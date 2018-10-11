lazy val catsVersion = "1.4.0"
lazy val disciplineVersion = "0.8"
lazy val scalaCheckVersion = "1.13.5"
lazy val scalaTestVersion = "3.0.5"
lazy val shapelessVersion = "2.3.3"

name := "Consolidate"

organization := "com.faacets"

scalaVersion := "2.12.7"

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation", "-language:implicitConversions", "-Ypartial-unification")

licenses := Seq("MIT" -> url("http://opensource.org/licenses/mit-license.php"))

homepage := Some(url("https://github.com/denisrosset/consolidate"))

bintrayRepository := "maven"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.typelevel" %% "cats-laws" % catsVersion % "test",
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.typelevel" %% "discipline" % disciplineVersion % "test"
)
