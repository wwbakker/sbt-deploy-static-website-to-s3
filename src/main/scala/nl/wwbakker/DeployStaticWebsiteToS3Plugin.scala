package nl.wwbakker

import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb
import sbt._

// info: https://www.scala-sbt.org/1.0/docs/Plugins-Best-Practices.html
object DeployStaticWebsiteToS3Plugin extends AutoPlugin {
  override def requires : Plugins = SbtWeb
  override def trigger : PluginTrigger = allRequirements

  object autoImport {
    lazy val DeployStaticWebsiteToS3 = config("deploy-static-website-to-s3")
    lazy val bucketName = settingKey[Option[String]]("Name of the S3 bucket to deploy the static website to. When hosting directly on a domain name, the bucket name should be the same (e.g. when you want to host your website on http://example.org, the bucketname should be example.org).")
    lazy val deployStaticWebsiteToS3 = taskKey[Unit]("Deploy the website to s3.")
  }
  import autoImport._
  lazy val baseDeployStaticWebsiteToS3Settings: Seq[Def.Setting[_]] = Seq(
    bucketName := None,
    deployStaticWebsiteToS3 := DeployStaticWebsiteToS3Action(stage.value)

  )
  // Should this be in its own configuration, or should we reuse another one (e.g. SbtWeb)?
  override lazy val projectSettings : Seq[Def.Setting[_]] = inConfig(DeployStaticWebsiteToS3)(baseDeployStaticWebsiteToS3Settings)
}

