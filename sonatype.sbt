import xerial.sbt.Sonatype._

publishMavenStyle := true
publishTo := sonatypePublishToBundle.value

sonatypeProjectHosting := Some(GitHubHosting("wwbakker", "sbt-deploy-static-website-to-s3", "wwbakker@gmail.com"))
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

developers := List(
  Developer(id = "wwbakker", name = "Wessel W. Bakker", email = "wwbakker@gmail.com", url = url("https://consulting.wwbakker.nl"))
)