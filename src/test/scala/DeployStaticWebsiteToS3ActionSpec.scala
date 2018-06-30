import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3
import nl.wwbakker.sbt.internal.{DeployStaticWebsiteToS3Action, RelativePath}
import org.scalatest.{FlatSpec, Matchers}

class DeployStaticWebsiteToS3ActionSpec extends FlatSpec with Matchers {
  val defaultDsw: DeployStaticWebsiteToS3Action = new DeployStaticWebsiteToS3Action {
    def s3: AmazonS3 = null
  }

  "DeployStaticWebsiteToS3Action.findRelativeToStagingDirectory" should "find the correct files" in {
    val cwd = Paths.get("src", "test", "resources", "testwebsite1").toFile
    assert(cwd.exists, "If this fails, the test is wrong")

    defaultDsw.filesRelativeToStagingDirectory(cwd) shouldBe Seq(
      RelativePath(Seq("css", "green", "style.css")),
      RelativePath(Seq("index.html"))
    )
  }

  "DeployStaticWebsiteToS3Action.objectsInBucketButNotStaging" should "return the correct keys" in {
    defaultDsw.objectsInBucketButNotInStaging(
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