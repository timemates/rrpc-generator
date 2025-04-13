plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
    id(libs.plugins.conventions.library.get().pluginId)
    `java-gradle-plugin`
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    // -- Project --
    implementation(projects.generatorCore)
    implementation(projects.pluginLoader)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- Libraries --
    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)

    // -- Tests --
    testImplementation(libs.junit.jupiter)
    testImplementation(gradleTestKit())
}


gradlePlugin {
    website = "https://github.com/timemates/rrpc-generator"
    vcsUrl = "https://github.com/timemates/rrpc-generator"

    pluginSourceSet(sourceSets.main.get())
    testSourceSets(sourceSets.test.get())

    plugins {
        create("rrpc-plugin") {
            id = "app.timemate.rrpc"
            displayName = "RRpc Code Generator"
            description = "Code Generator from .proto files to Kotlin code."
            tags = listOf("kotlin", "rsocket", "protobuf", "proto")

            implementationClass = "app.timemate.rrpc.gradle.RRpcGenerationGradlePlugin"
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "gradle-plugin",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Generator Gradle Plugin")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}


tasks.named<Test>("test") {
    useJUnitPlatform()
}