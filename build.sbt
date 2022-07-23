name := "sbt-deploy-static-website-to-s3"
description := "sbt plugin for deploying a static website to S3."
organization := "nl.wwbakker"
// Set to a -SNAPSHOT version when running scripted, non-snapshot version to release.
version := "1.4-SNAPSHOT"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.239"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"

enablePlugins(ScriptedPlugin)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
sbtPlugin := true
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// See sonatype.sbt for sonatype specific settings

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

// To publish/release:
// - Run `scripted` in sbt to test
// - Change version to non-snapshot version
// - Run `publishSigned` to create a local staging bundle
// - Run `sonatypeBundleRelease` to release
// - Fill in the gpg key.
// - Change back the version to the next -SNAPSHOT version
// - Commit code.
