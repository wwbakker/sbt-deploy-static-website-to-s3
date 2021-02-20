# sbt-deploy-static-website-to-s3
An sbt plugin for deploying a static website to s3 programmatically.

## Requirements.
- An AWS account with permissions for accessing and modifying S3 resources. Make sure you have configured your access
(for example by setting your credentials in `~/.aws/credentials` and your region in `~/.aws/config`.
- An SBT project, using the sbt-web plugin

## Configuring your project
- In `[yoursbtproject]/project/plugins.sbt` add the following:
```sbt
resolvers += Resolver.bintrayIvyRepo("wwbakker", "sbt-plugins")
addSbtPlugin("nl.wwbakker" % "sbt-deploy-static-website-to-s3" % "1.2")
// Add sbt-web if it isn't added to your project already.
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")
```
- SbtWeb needs to be enabled. When SbtWeb is enabled, this plugin's
 tasks will be automatically made available. To enable SbtWeb, add `.enablePlugins(SbtWeb)` to your `build.sbt`:
```sbt
lazy val root = (project in file("."))
  .settings(
    name := "your-project",
    organization := "org.example"
  ).enablePlugins(SbtWeb)
```
- Then in your project's `build.sbt` set the following properties
```sbt
// Configure your bucket name here. The bucketname will be visible in the URL.
// When you want to use S3 to host a website from your domain, be sure to mirror
// the domain name in your bucket name
(bucketName in DeployStaticWebsiteToS3) := Some("www.mywebsite.com")
// OPTIONAL: default is index.html
(indexDocument in DeployStaticWebsiteToS3) := "index.html"
// OPTIONAL: default is unset
(errorDocument in DeployStaticWebsiteToS3) := "error.html"
```

## Deploying your website
Once you have configured the plugin in your project you have the
following tasks available in your project:
- `deploy-static-website-to-s3:createS3Bucket`: Creates an empty s3 bucket, configured for use for static website.
- `deploy-static-website-to-s3:deployToS3`: Uploads your website's files and deletes files in the bucket that do not pertain to your site.
- `deploy-static-website-to-s3:undeployFromS3`: Removes all files from the configured S3 bucket.
- `deploy-static-website-to-s3:deleteS3Bucket`: Deletes the configured S3 bucket.

## Be careful
As this plugin manages your S3 buckets, be careful. Creating buckets and uploading files can
incur costs (AWS will charge for storing and serving data on S3).
Deleting your files on S3 without having a backup can cause you to lose files.