val scala3Version = "3.2.1"

resolvers +=
  "Clojars" at "https://repo.clojars.org"

lazy val root = project
  .in(file("."))
  .settings(
    name := "q2",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.clojure" % "clojure" % "1.11.1",
      "com.cnuernber" % "charred" % "1.018",
      "com.apicatalog" % "titanium-json-ld" % "1.3.1",
      
    )
  )
