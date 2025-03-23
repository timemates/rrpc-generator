plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
    id(libs.plugins.conventions.library.get().pluginId)
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
//    constraints {
//        api("org.timemates.rrpc.generator:kotlin:$version")
//    }

    // -- Project --
    implementation(projects.core)
    implementation(projects.pluginLoader)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- Libraries --
    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)
}


gradlePlugin {
    website = "https://github.com/rrpc-generator"
    vcsUrl = "https://github.com/rrpc-generator"

    plugins {
        create("rrpc-plugin") {
            id = "org.timemates.rrpc"
            displayName = "RRpc Code Generator"
            description = "Code Generator from .proto files to Kotlin code."
            tags = listOf("kotlin", "rsocket", "protobuf", "proto")

            implementationClass = "org.timemates.rrpc.gradle.RRpcGenerationGradlePlugin"
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "gradle-plugin",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Generator Gradle Plugin")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}