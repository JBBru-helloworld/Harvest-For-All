ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "harvest-for-all",
    libraryDependencies ++= {
      // Determine OS version of JavaFX binaries
      val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")   => "linux"
        case n if n.startsWith("Mac")     => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }

      // For Mac, determine the architecture
      val osArch = if (osName == "mac") {
        System.getProperty("os.arch") match {
          case arch if arch == "aarch64" || arch == "arm64" => "mac-aarch64"
          case _                                            => "mac"
        }
      } else {
        osName
      }

      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "21.0.4" classifier osArch)
    },

    // Install ScalaFX 21 for Scala 3, from Maven Repository
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "com.typesafe.play" %% "play-json" % "2.10.4" // For JSON serialization
    ),

    // Main class configurations
    Compile / mainClass := Some("harvestforall.HarvestForAllApp"),
    run / mainClass := Some("harvestforall.HarvestForAllApp"),

    // Alternative main classes for testing
    Compile / discoveredMainClasses := Seq(
      "harvestforall.HarvestForAllApp",
      "harvestforall.test.FarmGameTestApp"
    )
  )
//enable for sbt-assembly
//assembly / assemblyMergeStrategy := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard // Discard all META-INF files
//  case PathList("reference.conf")    => MergeStrategy.concat  // Concatenate config files
//  case PathList(ps @ _*) if ps.last.endsWith(".class") => MergeStrategy.first // Take the first class file
//  case _ => MergeStrategy.first // Apply first strategy to any other file
//}
