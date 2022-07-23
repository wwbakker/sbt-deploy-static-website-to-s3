lazy val root = (project in file("."))
  .settings(
    name := "test",
    organization := "nl.wwbakker"
  ).enablePlugins(SbtWeb)

DeployStaticWebsiteToS3 / bucketName := Some("deployplugintest")