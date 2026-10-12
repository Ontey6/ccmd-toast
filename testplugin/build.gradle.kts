plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.paper.yml)
    alias(libs.plugins.paper.run)
}


dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
    paperLibrary(kotlin("stdlib"))

    paperLibrary(libs.open.toast.api)
}

kotlin {
    jvmToolchain(25)
}

paper {
    name = "open-toast-testplugin"
    description = "Plugin used to try out the toasts of the api."
    author = "kxmpxtxnt"
    main = "fyi.pauli.opentoast.TestPlugin"
    loader = "fyi.pauli.opentoast.TestPluginLoader"

    apiVersion = libs.versions.minecraft.get()

    generateLibrariesJson = true
}

tasks {
    generatePaperPluginDescription {
        useDefaultCentralProxy()
    }
}
