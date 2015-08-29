name := "slack-bridge"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.github.gilbertw1" %% "slack-scala-client" % "0.1.2",
  "org.scalaz" %% "scalaz-core" % "7.1.3"
)
