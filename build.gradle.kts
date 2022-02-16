import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    java
}

group = "darkan"
version = "1.0.1"
description = "Darkan Launcher"

repositories {
    mavenLocal()
}

dependencies {

}

fun isNonStable(version: String): Boolean {
    return listOf("ALPHA", "BETA", "RC").any {
        version.toUpperCase().contains(it)
    }
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    build {
        finalizedBy("shadowJar")
    }

    processResources {
        val tokens = mapOf(
            "basedir"         to project.projectDir.path,
            "finalName"       to "Darkan",
            "artifact"        to "launcher",
            "project.version" to project.version,
            "project.group"   to project.group,
            "description"     to "Darkan launcher"
        )

        copy {
            from("${rootDir}/packr") {
                include("Info.plist")
            }
            from("${rootDir}/innosetup") {
                include("darkan.iss")
                include("darkan32.iss")
            }
            from("${rootDir}/appimage") {
                include("darkan.desktop")
            }
            into("${buildDir}/filtered-resources/")

            filter(ReplaceTokens::class, "tokens" to tokens)
            filteringCharset = "UTF-8"
        }

        doLast {
            copy {
                filter(ReplaceTokens::class, "tokens" to tokens)
                filteringCharset = "UTF-8"
            }
        }
    }

    jar {
        manifest {
            attributes(mutableMapOf("Main-Class" to "com.darkan.Loader"))
        }
    }

    shadowJar {
        archiveName = "darkan-shaded.jar"
    }
}