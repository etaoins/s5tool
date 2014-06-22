name := "s5tool"

version := "0.0.2"

scalaVersion := "2.11.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.8.0"

libraryDependencies += "org.ini4j" % "ini4j" % "0.5.2"

scalacOptions ++= Seq("-deprecation", "-feature")
