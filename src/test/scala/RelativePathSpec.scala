import java.io.File
import java.nio.file.{Files, Path, Paths}

import nl.wwbakker.sbt.internal.RelativePath
import org.scalatest.{FlatSpec, Matchers}

class RelativePathSpec extends FlatSpec with Matchers {
  "RelativePath" should "be created correctly" in {
    val cwd = new java.io.File(".").getAbsoluteFile
    val relativePath = Seq("src", "main", "nl", "wwbakker", "sbt", "internal", "RelativePath.scala")
    val relativePathSourceFile = Paths.get(".", relativePath :_*).toFile
    RelativePath(cwd, relativePathSourceFile) shouldBe RelativePath(relativePath)
  }

}