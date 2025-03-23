plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
    id(libs.plugins.conventions.jvm.library.get().pluginId)
}

dependencies {
    // -- Okio --
    implementation(libs.squareup.okio)

    // -- Generators --
    implementation(projects.core)
    implementation(projects.kotlin)

    // -- JNA --
    implementation(libs.net.java.jna)

    // -- KotlinX --
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.proto)

    // -- Integration Tests --
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(projects.kotlin)
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "generator-plugin-loader",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Code Generator Plugin Loader Library")
        description.set("RRpc Code Generator Plugin Loader Library.")
    }
}
