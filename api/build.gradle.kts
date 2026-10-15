plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.paperweight.userdev)
}

description = "Custom toasts for Paper servers"

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
    // If you contribute, use JitPack. I need this to make changes to the latest version
    compileOnly("ontey:CustomCommand:2.4.4")
}

kotlin {
    jvmToolchain(25)
}

val pack = tasks.register<Zip>("pack") {
    description = "Create resourcepack"
    from(rootProject.file("assets/resourcepack"))
    archiveFileName = "open-toast.zip"
    destinationDirectory = layout.buildDirectory.dir("pack")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks {
    processResources {
        from(pack)
    }
}
