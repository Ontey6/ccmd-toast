rootProject.name = "ccmd-toast"

include("api")
includeBuild("C:\\Users\\klett\\IdeaProjects\\CustomCommand")

pluginManagement {
	 repositories {
		  mavenCentral()
		  gradlePluginPortal()
		  maven("https://repo.papermc.io/repository/maven-public/")
	 }
}
