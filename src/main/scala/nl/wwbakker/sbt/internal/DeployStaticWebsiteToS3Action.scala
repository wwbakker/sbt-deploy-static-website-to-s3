package nl.wwbakker.sbt.internal

import java.io.File

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{DeleteObjectsRequest, S3ObjectSummary}
import nl.wwbakker.sbt.internal.RelativePath._

import scala.collection.JavaConverters._

trait DeployStaticWebsiteToS3Action {
  def s3 : AmazonS3

  def deploy(bucketName : String, stagingDirectory : File) : Unit = {
    val filesInStaging : Seq[RelativePath] = filesRelativeToStagingDirectory(stagingDirectory)
    uploadObjects(bucketName, stagingDirectory, filesInStaging)
    deleteObjects(bucketName, objectsInBucketButNotInStaging(objectsInBucket(bucketName), filesInStaging))
  }

  def undeploy(bucketName : String) : Unit = {
    deleteObjects(bucketName, objectsInBucket(bucketName))
  }

  def objectsInBucket(bucketName : String) : Seq[BucketKey] = {
    val bucketObjects : Seq[S3ObjectSummary] = s3.listObjectsV2(bucketName).getObjectSummaries.asScala
    bucketObjects.map(_.getKey)
  }
  def filesRelativeToStagingDirectory(stagingDirectory : File) : Seq[RelativePath] =
    listRecursively(stagingDirectory).map(RelativePath(stagingDirectory, _))

  def objectsInBucketButNotInStaging(objectsInBucket : Seq[BucketKey], filesInStaging : Seq[RelativePath]) : Seq[BucketKey] =
    objectsInBucket.filter(bucketKey => !filesInStaging.exists(_.bucketKey == bucketKey))

  def uploadObjects(bucketName: String, baseDirectory: File, files : Seq[RelativePath]) : Unit =
    files.foreach(path => s3.putObject(bucketName, path.bucketKey, path.file(baseDirectory)))

  def deleteObjects(bucketName: String, keys : Seq[BucketKey]) : Unit =
    s3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(keys.toArray:_*))

  def listRecursively(file: File) : List[File] =
    if (file.isFile)
      file :: Nil
    else
      file.listFiles.toList.flatMap(listRecursively)
}

