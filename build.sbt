scalaVersion := "2.12.8"

lazy val root = (project in file("."))
    .settings(
        name := "mocasys-frontend",
        Compile / scalaSource := baseDirectory.value / "src",
        libraryDependencies ++= Seq(
            "org.scala-js" %%% "scalajs-dom" % "0.9.6",
        ),
        // TODO: WebJars or traditional NPM infrastructure?
        // Also, Should we add an additional bundler?
        jsDependencies += ("org.webjars.npm" % "domvm" % "3.4.10"
                           / "dist/nano/domvm.nano.js"),
    )
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(JSDependenciesPlugin)
    .dependsOn(liwec)

// TODO: Git submodules or localPublish
lazy val liwec = ProjectRef(file("liwec"), "root")
