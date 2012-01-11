import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {

  // Repositories
  val akkaRepo = "Akka Repo" at "http://akka.io/repository"

  // Sub-project roxx
  val roxxProject = project("roxx", "roxx", new Roxxroject(_))
  class Roxxroject(info: ProjectInfo)
      extends DefaultWebProject(info) 
      with AkkaProject with Specs2Project with JettyProject {

    // Compile dependencies
    override val akkaActor = akkaModule("actor").withSources
    val akkaHttp = akkaModule("http").withSources
    val casbah = "com.mongodb.casbah" %% "casbah-core" % "2.1.5-1" withSources
    val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.2.2" withSources
    val commonsIo = "commons-io" % "commons-io" % "2.0.1" withSources
    val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.6" withSources
    val logback = "ch.qos.logback" % "logback-classic" % "0.9.28"
    val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.1" withSources
    val sjson = "net.debasishg" %% "sjson" % "0.13" withSources

    // Test dependencies
    val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test"
    val scalaCheck = "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"
    val specs2 = "org.specs2" %% "specs2" % "1.5" % "test"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.7.8" withSources

    // Provided dependencies
    val javaxServlet30 = "org.mortbay.jetty" % "servlet-api" % "3.0.20100224" % "provided"    
  }

  // Sub-project roxx-it
  val roxxItProject = project("roxx-it", "roxx-it", new RoxxItProject(_), roxxProject)
  class RoxxItProject(info: ProjectInfo) 
      extends DefaultWebProject(info) 
      with Specs2Project with JettyProject {
    override val webappPath = roxxProject.webappPath
    override lazy val jettyRun = task { None }
    override val testAction = super.testAction dependsOn (super.jettyStopAction && super.jettyRunAction)
  }

  trait Specs2Project extends BasicScalaProject {
    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  }

  trait JettyProject extends DefaultWebProject {
    // Test dependencies
    //val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "test"
    val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "8.0.0.M2" % "test"
    // Webapp settings for JRebel
    override def jettyWebappPath = webappPath
    override def scanDirectories = Nil
  }
}
