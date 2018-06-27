name := "sbt-deploy-static-website-to-s3"
description := "sbt plugin for deploying a static website to S3."
organization := "nl.wwbakker"

licenses += ("MIT License", url("http://opensource.org/licenses/MIT"))
sbtPlugin := true
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
// Set to a -SNAPSHOT version when running scripted, non-snapshot version to release.
version := "1.0-SNAPSHOT"

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false