package nl.wwbakker.sbt.internal

import java.io.File

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import nl.wwbakker.sbt.internal.exceptions.DeployStaticWebsiteToS3Exception
import sbt.util.Logger

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait DeployStaticWebsiteToS3Action {
  def s3: AmazonS3

  def deploy(bucketName: String, stagingDirectory: File)(implicit logger: Logger): Unit = {
    val filesInStaging: Seq[RelativePath] = filesRelativeToStagingDirectory(stagingDirectory)
    val objectsInS3Bucket : Seq[BucketObject] = objectsInBucket(bucketName)
    val modifiedFilesInStaging = skipUnmodifiedObjects(filesInStaging, objectsInS3Bucket, stagingDirectory)
    uploadObjects(bucketName, stagingDirectory, modifiedFilesInStaging)
    deleteObjects(bucketName, objectsInBucketButNotInStaging(objectsInS3Bucket, filesInStaging))
  }

  def undeploy(bucketName: String)(implicit logger: Logger): Unit = {
    deleteObjects(bucketName, objectsInBucket(bucketName))
  }

  protected def skipUnmodifiedObjects(filesInStaging: Seq[RelativePath], objectsInS3Bucket : Seq[BucketObject], stagingDirectory: File)(implicit logger: Logger) : Seq[RelativePath] = {
    val modifiedFilesInStaging : Seq[RelativePath] = modifiedFiles(objectsInS3Bucket, filesInStaging, stagingDirectory)
    val unmodifiedFiles = filesInStaging.diff(modifiedFilesInStaging)
    unmodifiedFiles.foreach(relativePath => logger.info(s"Skipped ${relativePath.bucketKey} as nothing has changed."))
    modifiedFilesInStaging
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

  protected def deleteObjects(bucketName: String, bucketObjects: Seq[BucketObject])(implicit logger: Logger): Unit =
    if (bucketObjects.nonEmpty)
      Try(s3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(bucketObjects.map(_.key).toArray: _*))) match {
        case Success(_) if bucketObjects.nonEmpty =>
          logger.info(s"${bucketObjects.length} objects deleted from S3. ")
        case Success(_) =>
          logger.info(s"No files to delete from S3 bucket.")
        case Failure(e) =>
          val errorMessage = s"Error while deleting objects from the S3 bucket '$bucketName'."
          logger.error(s"$errorMessage\n${e.getMessage}")
          throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
      }
    else
      logger.info("No objects in the S3 bucket to delete.")



  protected def objectsInBucket(bucketName: String)(implicit logger: Logger): Seq[BucketObject] = {
    Try(s3.listObjectsV2(bucketName).getObjectSummaries.asScala.map(summary => BucketObject(summary.getKey, summary.getETag))) match {
      case Success(result) => result
      case Failure(e) =>
        val errorMessage = s"Error while listing object from S3 bucket '$bucketName'"
        logger.error(s"$errorMessage\n${e.getMessage}")
        throw new DeployStaticWebsiteToS3Exception(errorMessage, e)
    }
  }

  protected def filesRelativeToStagingDirectory(stagingDirectory: File): Seq[RelativePath] =
    listRecursively(stagingDirectory).map(RelativePath(stagingDirectory, _))

  protected def objectsInBucketButNotInStaging(objectsInBucket: Seq[BucketObject], filesInStaging: Seq[RelativePath]): Seq[BucketObject] =
    objectsInBucket.filter(bucketObject => !filesInStaging.exists(_.bucketKey == bucketObject.key))

  protected def modifiedFiles(objectsInBucket: Seq[BucketObject], filesInStaging: Seq[RelativePath], baseDirectory : File) : Seq[RelativePath] =
    filesInStaging
      .zip(filesInStaging.map(relativePath => Md5Hash.computeHash(relativePath.file(baseDirectory))))
      .filter{
        case (relativePath, hash) =>
          !objectsInBucket.exists(bucketObject => bucketObject.key == relativePath.bucketKey && bucketObject.hash.toLowerCase == hash)}
      .map(_._1)

  private def listRecursively(file: File): List[File] =
    if (file.isFile)
      file :: Nil
    else
      file.listFiles.toList.flatMap(listRecursively)
}

