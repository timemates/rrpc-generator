plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
    application
    alias(libs.plugins.graalvm.native)
}

dependencies {
    // -- CLI --
    implementation(libs.clikt.core)

    // -- Okio --
    implementation(libs.squareup.okio)

    // -- Generators --
    implementation(projects.generatorCore)
    implementation(projects.pluginLoader)

    // -- JNA --
    implementation(libs.net.java.jna)

    // -- KotlinX --
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.proto)

    // -- Integration Tests --
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

application {
    applicationName = "rrgcli"
    mainClass = "app.timemate.rrpc.generator.cli.RRpcGeneratorMain"
}

graalvmNative {
    binaries {
        named("main") {
            mainClass = "app.timemate.rrpc.generator.cli.MainKt"
            buildArgs.addAll(
                "--initialize-at-build-time=kotlin.DeprecationLevel",
                "-H:ReflectionConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/reflect-config.json")}",
                "-H:ResourceConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/resource-config.json")}",
                "-H:Name=rrgcli"
            )
            useFatJar = true
        }
        named("test") {
            buildArgs.addAll(
                "--verbose",
                "-O0",
                "-H:Name=rrgcli",
            )
            useFatJar = true
        }
    }
}