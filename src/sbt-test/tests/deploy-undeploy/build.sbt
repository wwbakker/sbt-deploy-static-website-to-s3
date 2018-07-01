lazy val root = (project in file("."))
  .settings(
    name := "test",
    organization := "nl.wwbakker"
  ).enablePlugins(SbtWeb)

(bucketName in DeployStaticWebsiteToS3) := Some("deployplugintest")