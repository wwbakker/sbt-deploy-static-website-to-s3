package nl.wwbakker.sbt.internal

import java.io.File

import scala.annotation.tailrec

case class RelativePath(pathElements : Seq[String]) {
  def file(baseDirectory : File) = new File((baseDirectory.getPath +: pathElements).mkString(File.separator))
  def bucketKey : RelativePath.BucketKey = pathElements.mkString("/")
}

object RelativePath {
  type BucketKey = String
  def apply(stagingDirectory : File, file : File) : RelativePath =
    RelativePath(findRelativePathElements(stagingDirectory, file, Nil))

  @tailrec
  def findRelativePathElements(stagingDirectory : File, currentFile : File, accumulator: List[String]) : List[String] =
    if (currentFile.getCanonicalPath == stagingDirectory.getCanonicalPath)
      accumulator
    else
      findRelativePathElements(stagingDirectory, currentFile.getParentFile, currentFile.getName :: accumulator)

}