package nl.wwbakker.sbt.internal

import java.io.File

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{DeleteObjectsRequest, S3ObjectSummary}
import nl.wwbakker.sbt.internal.RelativePath._
import nl.wwbakker.sbt.internal.exceptions.DeployStaticWebsiteToS3Exception
import sbt.util.Logger

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait DeployStaticWebsiteToS3Action {
  def s3: AmazonS3

  def deploy(bucketName: String, stagingDirectory: File)(implicit logger: Logger): Unit = {
    val filesInStaging: Seq[RelativePath] = filesRelativeToStagingDirectory(stagingDirectory)
    uploadObjects(bucketName, stagingDirectory, filesInStaging)
    deleteObjects(bucketName, objectsInBucketButNotInStaging(objectsInBucket(bucketName), filesInStaging))
  }

  def undeploy(bucketName: String)(implicit logger: Logger): Unit = {
    deleteObjects(bucketName, objectsInBucket(bucketName))
  }

  protected def uploadObjects(bucketName: String, baseDirectory: File, files: Seq[RelativePath])(implicit logger: Logger): Unit =
    Try(files.foreach { path =>
      logger.info(s"Uploading ${path.bucketKey} to S3.")
      s3.putObject(bucketName, path.bucketKey, path.file(baseDirectory))
    }) match {
      case Success(_) if files.nonEmpty =>
        logger.info(s"${files.length} files uploaded to '$bucketName'.")
      case Success(_) =>
        logger.info("No files available for upload to S3.")
      case Failure(e) =>
        val errorMessage = s"Error while uploading files to S3 bucket '$bucketName'."
        logger.error(s"$errorMessage\n${e.getMessage}")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }

  protected def deleteObjects(bucketName: String, keys: Seq[BucketKey])(implicit logger: Logger): Unit =
    if (keys.nonEmpty)
      Try(s3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(keys.toArray: _*))) match {
        case Success(_) if keys.nonEmpty =>
          logger.info(s"${keys.length} objects deleted from S3. ")
        case Success(_) =>
          logger.info(s"No files to delete from S3 bucket.")
        case Failure(e) =>
          val errorMessage = s"Error while deleting objects from the S3 bucket '$bucketName'."
          logger.error(s"$errorMessage\n${e.getMessage}")
          throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
      }
    else
      logger.info("No objects in the S3 bucket to delete.")


  protected def objectsInBucket(bucketName: String)(implicit logger: Logger): Seq[BucketKey] = {
    Try(s3.listObjectsV2(bucketName).getObjectSummaries.asScala.map(_.getKey)) match {
      case Success(result) => result
      case Failure(e) =>
        val errorMessage = s"Error while listing object from S3 bucket '$bucketName'"
        logger.error(s"$errorMessage\n${e.getMessage}")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }
  }

  protected def filesRelativeToStagingDirectory(stagingDirectory: File): Seq[RelativePath] =
    listRecursively(stagingDirectory).map(RelativePath(stagingDirectory, _))

  protected def objectsInBucketButNotInStaging(objectsInBucket: Seq[BucketKey], filesInStaging: Seq[RelativePath]): Seq[BucketKey] =
    objectsInBucket.filter(bucketKey => !filesInStaging.exists(_.bucketKey == bucketKey))

  private def listRecursively(file: File): List[File] =
    if (file.isFile)
      file :: Nil
    else
      file.listFiles.toList.flatMap(listRecursively)
}

