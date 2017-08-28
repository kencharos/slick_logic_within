name := "slick_logic_within"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.typelevel" %% "cats-core" % "1.0.0-MF",
  "org.slf4j" % "slf4j-nop" % "1.7.10",
  "com.h2database" % "h2" % "1.4.196" % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test"

)