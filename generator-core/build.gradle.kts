plugins {
    id(libs.plugins.conventions.jvm.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    explicitApi()
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    api(projects.schema)
    api(projects.pluginApi)

    // -- Serialization --
    implementation(libs.kotlinx.serialization.proto)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    implementation(libs.squareup.wire.schema)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.okio)

    // -- Test --
    testImplementation(libs.kotlin.test)
    testImplementation(libs.squareup.okio.fakeFs)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "generator-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Code Generator Core")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}