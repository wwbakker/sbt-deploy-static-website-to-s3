package nl.wwbakker.sbt

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb
import nl.wwbakker.sbt.internal.{BucketManager, DeployStaticWebsiteToS3Action}
import sbt.Keys._
import sbt._

// info: https://www.scala-sbt.org/1.0/docs/Plugins-Best-Practices.html
object DeployStaticWebsiteToS3Plugin extends AutoPlugin {
  override def requires: Plugins = SbtWeb

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val DeployStaticWebsiteToS3 = config("deploy-static-website-to-s3")
    lazy val bucketName = settingKey[Option[String]]("Name of the S3 bucket to deploy the static website to. When hosting directly on a domain name, the bucket name should be the same (e.g. when you want to host your website on http://example.org, the bucketname should be example.org).")
    lazy val indexDocument = settingKey[String]("Sets the index document of your website.")
    lazy val errorDocument = settingKey[Option[String]]("This document is returned when an error occurs.")
    lazy val createS3Bucket = taskKey[Unit]("Create a new S3 bucket configured for hosting your static website.")
    lazy val deleteS3Bucket = taskKey[Unit]("Delete the configured s3 bucket.")
    lazy val deployToS3 = taskKey[Unit]("Deploy the static website to an existing s3 bucket.")
    lazy val undeployFromS3 = taskKey[Unit]("Undeploy the static website from an existing s3 bucket.")
  }

  import autoImport._

  lazy val baseDeployStaticWebsiteToS3Settings: Seq[Def.Setting[_]] = Seq(
    bucketName := None,
    indexDocument := "index.html",
    errorDocument := None,
    deployToS3 := {
      bucketName.value match {
        case Some(bn) =>
          DefaultDeployStaticWebsiteToS3Action.deploy(bn, stage.value)(streams.value.log)
        case None =>
          streams.value.log.warn("SBT property 'bucketName' not set. Skipping s3 deployment.")
      }
    },
    undeployFromS3 := {
      bucketName.value match {
        case Some(bn) =>
          DefaultDeployStaticWebsiteToS3Action.undeploy(bn)(streams.value.log)
        case None =>
          streams.value.log.warn("SBT property 'bucketName' not set. Skipping s3 undeploy.")
      }
    },
    createS3Bucket := {
      bucketName.value match {
        case Some(bn) =>
          DefaultBucketManager.createBucketForStaticWebsite(bn, indexDocument.value, errorDocument.value)(streams.value.log)
        case None =>
          streams.value.log.warn("SBT property 'bucketName' not set. Skipping s3 bucket creation.")
      }
    },
    deleteS3Bucket := {
      bucketName.value match {
        case Some(bn) =>
          DefaultBucketManager.deleteBucket(bn)(streams.value.log)
        case None =>
          streams.value.log.warn("SBT property 'bucketName' not set. Skipping s3 bucket deletion.")
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

object DefaultBucketManager extends BucketManager {
  override lazy val s3: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
}