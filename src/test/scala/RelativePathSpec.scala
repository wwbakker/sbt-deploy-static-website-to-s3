import nl.wwbakker.sbt.internal.RelativePath
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths

class RelativePathSpec extends AnyFlatSpec with Matchers {
  "RelativePath" should "be created correctly" in {
    val cwd = new java.io.File(".").getAbsoluteFile
    val relativePath = Seq("src", "main", "nl", "wwbakker", "sbt", "internal", "RelativePath.scala")
    val relativePathSourceFile = Paths.get(".", relativePath :_*).toFile
    RelativePath(cwd, relativePathSourceFile) shouldBe RelativePath(relativePath)
  }

}