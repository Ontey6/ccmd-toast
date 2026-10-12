plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.paperweight.userdev)

    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "pauli"
            credentials {
                username = property("repo.pauli.username").toString()
                password = property("repo.pauli.password").toString()
            }
            setUrl("https://repo.pauli.fyi/releases")
        }
    }

    publications {
        register<MavenPublication>(project.name) {
            from(components["java"])

            this.groupId = project.group.toString()
            this.artifactId = "open-toast-${project.name}"
            this.version = rootProject.version.toString()

            pom {
                name.set(project.name)
                description.set(project.description)

                developers {
                    developer {
                        name = "kxmpxtxnt"
                    }
                }

                licenses {
                    license {
                        name.set("GNU General Public License 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }

                url.set("https://github.com/kxmpxtxnt/open-toast")

                scm {
                    connection.set("scm:git:git://github.com/kxmpxtxnt/open-toast.git")
                    url.set("https://github.com/kxmpxtxnt/open-toast/tree/main")
                }
            }
        }
    }
}

description = "Custom toasts for Paper servers"

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
}

kotlin {
    jvmToolchain(25)
}

val pack = tasks.register<Zip>("pack") {
    description = "Create resourcepack"
    from("resourcepack")
    archiveFileName = "open-toast-pack.zip"
    destinationDirectory = layout.buildDirectory.dir("pack")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks {
    processResources {
        from(pack)
    }
}
