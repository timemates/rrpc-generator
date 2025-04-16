plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    explicitApi()
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Serialization --
    commonMainImplementation(libs.kotlinx.serialization.proto)

    // -- Coroutines --
    commonMainImplementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    commonMainImplementation(libs.squareup.okio)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "schema-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Proto Schema Library")
        description.set("The schema that is used across rrpc-kotlin and rrpc-generator.")
    }
}