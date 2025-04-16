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

    // -- Serialization --
    implementation(libs.kotlinx.serialization.proto)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    implementation(libs.squareup.wire.schema)
    api(libs.squareup.okio)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "generator-plugin-api",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Code Generator Core")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}