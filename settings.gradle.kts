rootProject.name = "ccmd-toast"

include("api", "testplugin")
includeBuild("C:\\Users\\klett\\IdeaProjects\\CustomCommand")

pluginManagement {
	 repositories {
		  mavenCentral()
		  gradlePluginPortal()
		  maven("https://repo.papermc.io/repository/maven-public/")
	 }
}
