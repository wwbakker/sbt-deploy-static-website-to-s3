import java.io.File
import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3
import nl.wwbakker.sbt.internal.{BucketObject, DeployStaticWebsiteToS3Action, RelativePath}
import org.scalatest.{FlatSpec, Matchers}

class DeployStaticWebsiteToS3ActionSpec extends FlatSpec with Matchers {

  object TestDeployStaticWebsiteToS3Action extends DeployStaticWebsiteToS3Action {
    def s3: AmazonS3 = null

    override def filesRelativeToStagingDirectory(stagingDirectory: File): Seq[RelativePath] =
      super.filesRelativeToStagingDirectory(stagingDirectory)

    override def objectsInBucketButNotInStaging(objectsInBucket: Seq[BucketObject], filesInStaging: Seq[RelativePath]): Seq[BucketObject] =
      super.objectsInBucketButNotInStaging(objectsInBucket, filesInStaging)

    override def modifiedFiles(objectsInBucket: Seq[BucketObject], filesInStaging: Seq[RelativePath], baseDirectory: File): Seq[RelativePath] =
      super.modifiedFiles(objectsInBucket, filesInStaging, baseDirectory)
  }

  "DeployStaticWebsiteToS3Action.findRelativeToStagingDirectory" should "find the correct files" in {
    val cwd = Paths.get("src", "test", "resources", "testwebsite1").toFile
    assert(cwd.exists, "If this fails, the test is wrong")

    TestDeployStaticWebsiteToS3Action.filesRelativeToStagingDirectory(cwd) shouldBe Seq(
      RelativePath(Seq("css", "green", "style.css")),
      RelativePath(Seq("index.html"))
    )
  }

  "DeployStaticWebsiteToS3Action.objectsInBucketButNotStaging" should "return the correct keys" in {
    TestDeployStaticWebsiteToS3Action.objectsInBucketButNotInStaging(
      objectsInBucket = Seq(
        BucketObject("not/in/staging", ""),
        BucketObject("css/green/style.css", "")
      ),
      filesInStaging = Seq(
        RelativePath(Seq("css", "green", "style.css"))
      )
    ) shouldBe Seq(BucketObject("not/in/staging", ""))
  }

  "DeployStaticWebsiteToS3Action.modifiedFiles" should "return the correct files" in {
    val cwd = Paths.get("src", "test", "resources", "testwebsite1").toFile
    assert(cwd.exists, "If this fails, the test is wrong")

    TestDeployStaticWebsiteToS3Action.modifiedFiles(
      objectsInBucket = Seq(
        BucketObject("not/in/staging", ""),
        BucketObject("css/green/style.css", "55eb5c2cc6f764903802e9d93a75c90b"),
        BucketObject("index.html", "")
      ),
      filesInStaging = Seq(
        RelativePath(Seq("css", "green", "style.css")),
        RelativePath(Seq("index.html"))
      )
      ,cwd) shouldBe Seq(
      RelativePath(Seq("index.html"))
    )
  }
}