name := "Consolidate"

organization := "com.faacets"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")

licenses := Seq("MIT" -> url("http://opensource.org/licenses/mit-license.php"))

homepage := Some(url("https://github.com/denisrosset/consolidate"))

initialCommands in console := """import com.faacets.consolidate._; import algebra._; import syntax.all._"""

bintrayRepository := "maven"
