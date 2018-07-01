package nl.wwbakker.sbt.internal

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration
import nl.wwbakker.sbt.internal.exceptions.DeployStaticWebsiteToS3Exception
import sbt.util.Logger

import scala.util.{Failure, Success, Try}

trait BucketManager {
  def s3: AmazonS3

  def createBucketForStaticWebsite(bucketName : String, indexDocument : String, errorDocument : Option[String])(implicit logger : Logger) : Unit = {
    createBucket(bucketName)
    setStaticWebsiteProperties(bucketName, indexDocument, errorDocument)
    setBucketPolicyToPublicReadAccess(bucketName)
    showUrl(bucketName)
  }

  def deleteBucket(bucketName : String)(implicit logger : Logger) : Unit = {
    Try(s3.deleteBucket(bucketName)) match {
      case Success(_) =>
        logger.info(s"Bucket '$bucketName' successfully deleted")
      case Failure(e) =>
        val errorMessage = s"Failed to delete '$bucketName'."
        logger.error(s"$errorMessage\n${e.getMessage}")
        logger.warn("WARNING: Not deleting the bucket can keep costing you money.")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }
  }

  protected def createBucket(bucketName : String)(implicit logger : Logger): Unit = {
    logger.info(s"Creating bucket '$bucketName'.")
    if (s3.doesBucketExistV2(bucketName))
      logger.error(s"Bucket '$bucketName' already exists. Cannot create it.")
    else
      Try(s3.createBucket(bucketName)) match {
        case Success(_) =>
          logger.info(s"Bucket '$bucketName' created.")
          logger.warn("WARNING: Having an S3 bucket can cost you money. Make sure to delete the bucket if you don't use it anymore!")
        case Failure(e) =>
          val errorMessage = s"Failed to create '$bucketName'."
          logger.error(s"$errorMessage\n${e.getMessage}")
          throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
      }
  }

  protected def setStaticWebsiteProperties(bucketName : String, indexDocument : String, errorDocument : Option[String])(implicit logger : Logger) : Unit = {
    logger.info(s"Setting static website properties.")
    Try(s3.setBucketWebsiteConfiguration(bucketName, new BucketWebsiteConfiguration(indexDocument, errorDocument.orNull))) match {
      case Success(_) =>
        logger.info("Static website properties set.")
      case Failure(e) =>
        val errorMessage = s"Failed to set static website properties for '$bucketName'."
        logger.error(s"$errorMessage\n${e.getMessage}")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }
  }

  protected def setBucketPolicyToPublicReadAccess(bucketName : String)(implicit logger : Logger) : Unit = {
    logger.info(s"Setting access policy to public.")
    Try(s3.setBucketPolicy(bucketName, publicBucketPolicyText(bucketName))) match {
      case Success(_) =>
        logger.info("Access policy set to public.")
      case Failure(e) =>
        val errorMessage = s"Failed to access policy for '$bucketName'."
        logger.error(s"$errorMessage\n${e.getMessage}")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }
  }

  protected def showUrl(bucketName : String)(implicit logger : Logger) : Unit = {
    logger.info(s"Website address: http://$bucketName.s3-website-${s3.getRegionName}.amazonaws.com")
  }

  def publicBucketPolicyText(bucketName : String): String =
    s"""{
      |    "Version": "2012-10-17",
      |    "Statement": [
      |        {
      |            "Sid": "PublicReadGetObject",
      |            "Effect": "Allow",
      |            "Principal": "*",
      |            "Action": "s3:GetObject",
      |            "Resource": "arn:aws:s3:::$bucketName/*"
      |        }
      |    ]
      |}""".stripMargin
}
