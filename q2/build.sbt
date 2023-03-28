val theScalaVersion = "2.13.10"

resolvers +=
  "Clojars" at "https://repo.clojars.org"

lazy val root = project
  .in(file("."))
  .settings(
    name := "quantum_nest",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := theScalaVersion,
    // seq(clojure.settings :_*),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.clojure" % "clojure" % "1.11.1",
      "com.cnuernber" % "charred" % "1.018",
      "com.apicatalog" % "titanium-json-ld" % "1.3.1",
      "net.liftweb" %% "lift-json" % "3.5.0",
      "net.liftweb" %% "lift-util" % "3.5.0"
    )
  )
