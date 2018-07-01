import java.io.File
import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3
import nl.wwbakker.sbt.internal.RelativePath.BucketKey
import nl.wwbakker.sbt.internal.{DeployStaticWebsiteToS3Action, RelativePath}
import org.scalatest.{FlatSpec, Matchers}

class DeployStaticWebsiteToS3ActionSpec extends FlatSpec with Matchers {
  object TestDeployStaticWebsiteToS3Action extends DeployStaticWebsiteToS3Action {
    def s3: AmazonS3 = null
    override def filesRelativeToStagingDirectory(stagingDirectory: File): Seq[RelativePath] = super.filesRelativeToStagingDirectory(stagingDirectory)
    override def objectsInBucketButNotInStaging(objectsInBucket: Seq[BucketKey], filesInStaging: Seq[RelativePath]): Seq[BucketKey] = super.objectsInBucketButNotInStaging(objectsInBucket, filesInStaging)
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
        "not/in/staging",
        "css/green/style.css"
      ),
      filesInStaging = Seq(
        RelativePath(Seq("css", "green", "style.css"))
      )
    ) shouldBe Seq("not/in/staging")
  }

}