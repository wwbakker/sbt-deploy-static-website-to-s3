package nl.wwbakker.sbt.internal

import java.io.{File, FileInputStream}
import java.security.{DigestInputStream, MessageDigest}

// Code retrieved from https://stackoverflow.com/questions/41642595/scala-file-hashing
object Md5Hash {
  // Compute a hash of a file
  // The output of this function should match the output of running "md5 -q <file>"
  def computeHash(file : File): String = {
    val buffer = new Array[Byte](8192)
    val md5 = MessageDigest.getInstance("MD5")

    val dis = new DigestInputStream(new FileInputStream(file), md5)
    try {
      while (dis.read(buffer) != -1) {}
    } finally {
      dis.close()
    }

    md5.digest.map("%02x".format(_)).mkString
  }

}
