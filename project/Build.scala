import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.scalamacros",
    scalaVersion := "2.10.0",
    scalacOptions ++= Seq("-Ywarn-numeric-widen", "-Yno-adapted-args", "-Ywarn-all")
  )
}

object DancingBuild extends Build {
  import BuildSettings._

  /*lazy val root = Project(
    "root",
    file("core"),
    settings = buildSettings
  ) aggregate(macros, core)*/

   lazy val scalaHomeLoc = (baseDirectory, scalaHome) { (f, sHome) =>  
     val props = new java.util.Properties()
     IO.load(props, f / "local.properties")
     val x = props.getProperty("scala.instrumented.home")
     if (x == null) {
       System.err.println("Failed to locate custom scala jars")
       System.exit(1)
       null
     } else {
       println("Using custom scala jars at " + x)
       file(x)
     }
   }

  lazy val macros = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies += ("com.chuusai" %% "shapeless" % "1.2.3"),
      scalaHome <<= scalaHomeLoc { scalaLocation =>
        Some(scalaLocation)
      },
      unmanagedBase <<= scalaHomeLoc { scalaLocation =>
        scalaLocation
      } 
    )
  )

  lazy val core = Project(
    "core",
    file("core"),
    settings = buildSettings ++ Seq(
      scalaHome <<= scalaHomeLoc { scalaLocation =>
        Some(scalaLocation)
      },
      unmanagedBase <<= scalaHomeLoc { scalaLocation =>
        scalaLocation
      }
    )
  ) dependsOn(macros)
}
