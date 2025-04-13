plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
    id(libs.plugins.conventions.jvm.library.get().pluginId)
}

dependencies {
    // -- Project --
    api(projects.pluginApi)

    // -- FileSystem --
    implementation(libs.squareup.okio)

    // -- Proto Parser --
    implementation(libs.squareup.wire.schema)

    // -- JNA --
    implementation(libs.net.java.jna)

    // -- KotlinX --
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.proto)

    // -- Integration Tests --
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "generator-plugin-loader",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Code Generator Plugin Loader Library")
        description.set("RRpc Code Generator Plugin Loader Library.")
    }
}
