name := "s5tool"

version := "0.0.1"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.github.scopt" %% "scopt" % "2.1.0"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"
      
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.3.14"
