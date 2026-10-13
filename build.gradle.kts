plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.paperweight.userdev) apply false
    alias(libs.plugins.paper.yml) apply false
    alias(libs.plugins.paper.run) apply false

    alias(libs.plugins.spotless)
}

allprojects {
    group = "fyi.pauli"
    version = "0.1.4"

    repositories {
        mavenCentral()
        maven("https://repo.pauli.fyi/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

spotless {
    java {
        target("*/src/**/*.java")
        licenseHeaderFile(file("HEADER.txt"))
    }

    kotlin {
        target("*/src/**/*.kt")
        licenseHeaderFile(file("HEADER.txt"))
    }
}