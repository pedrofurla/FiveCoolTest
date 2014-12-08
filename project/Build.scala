import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "fivecooltest"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.scalaz" %% "scalaz-core" % "7.1.0",
      "joda-time" % "joda-time" % "2.6"
      // Add your project dependencies here,
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
