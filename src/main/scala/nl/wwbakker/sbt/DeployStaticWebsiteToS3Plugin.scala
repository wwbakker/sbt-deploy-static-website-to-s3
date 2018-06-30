package nl.wwbakker.sbt

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb
import nl.wwbakker.sbt.internal.DeployStaticWebsiteToS3Action
import sbt.Keys._
import sbt._

// info: https://www.scala-sbt.org/1.0/docs/Plugins-Best-Practices.html
object DeployStaticWebsiteToS3Plugin extends AutoPlugin {
  override def requires: Plugins = SbtWeb

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val DeployStaticWebsiteToS3 = config("deploy-static-website-to-s3")
    lazy val bucketName = settingKey[Option[String]]("Name of the S3 bucket to deploy the static website to. When hosting directly on a domain name, the bucket name should be the same (e.g. when you want to host your website on http://example.org, the bucketname should be example.org).")
    lazy val deployToS3 = taskKey[Unit]("Deploy the static website to s3.")
    lazy val undeployFromS3 = taskKey[Unit]("Undeploy the static website from s3.")
  }

  import autoImport._

  lazy val baseDeployStaticWebsiteToS3Settings: Seq[Def.Setting[_]] = Seq(
    bucketName := None,
    deployToS3 := {
      bucketName.value match {
        case Some(bn) =>
          DefaultDeployStaticWebsiteToS3Action.deploy(bn, stage.value)
        case None =>
          streams.value.log("Property 'bucketName' not set. Skipping s3 deployment.")
      }
    },
    undeployFromS3 := {
      bucketName.value match {
        case Some(bn) =>
          DefaultDeployStaticWebsiteToS3Action.undeploy(bn)
        case None =>
          streams.value.log("Property 'bucketName' not set. Skipping s3 undeploy.")
      }
    }

  )
  // Should this be in its own configuration, or should we reuse another one (e.g. SbtWeb)?
  override lazy val projectSettings: Seq[Def.Setting[_]] =
    inConfig(DeployStaticWebsiteToS3)(baseDeployStaticWebsiteToS3Settings)
}

object DefaultDeployStaticWebsiteToS3Action extends DeployStaticWebsiteToS3Action {
  override lazy val s3: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
}