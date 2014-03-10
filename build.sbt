name := """onsstat-play"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  // Select Play modules
  jdbc,      // The JDBC connection pool and the play.api.db API
  anorm,     // Scala RDBMS Library
 javaJdbc,  // Java database API
  javaEbean, // Java Ebean plugin
  javaJpa,   // Java JPA plugin
  //filters,   // A set of built-in filters
  javaCore,  // The core Java API
  // WebJars pull in client-side web libraries
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  // Add your own project dependencies in the form:
  // "group" % "artifact" % "version"
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.avaje.ebeanorm" % "avaje-ebeanorm-api" % "3.1.1"
  //"org.hibernate.javax.persistence" %% "hibernate-jpa-2.0-api" % "1.0.0.Final"
)

play.Project.playScalaSettings
