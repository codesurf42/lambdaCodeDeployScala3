import Dependencies._

lazy val awsVersion = "1.11.192"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "awsLambdaScala",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
      "com.amazonaws" % "aws-java-sdk-bom" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
      scalaTest % Test
    ),
    assemblyJarName in assembly := "awsLambda.jar"
  )

