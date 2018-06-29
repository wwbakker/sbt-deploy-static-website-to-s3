package nl.wwbakker

import java.io.File

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.S3ObjectSummary

import scala.collection.JavaConverters._

object DeployStaticWebsiteToS3Action {
  def apply(stagingDirectory : File) : Unit = ???

  def go(bucketName : String): Unit = {
    val s3 = AmazonS3ClientBuilder.defaultClient()
    val bucketObjects : Seq[S3ObjectSummary] = s3.listObjectsV2(bucketName).getObjectSummaries.asScala
    bucketObjects.foreach(bo => println(bo.getKey))
    s3.putObject("consulting.wwbakker.nl","test/test.txt","test")
  }


  def listRecursively(file: File) : List[File] =
    if (file.isFile)
      file :: Nil
    else
      file.listFiles.toList.flatMap(listRecursively)
}