rootProject.name = "open-toast"

include(
    "api",
    "testplugin",
)

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
