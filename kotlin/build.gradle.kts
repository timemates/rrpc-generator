import org.gradle.internal.os.OperatingSystem

plugins {
    id(libs.plugins.conventions.jvm.library.get().pluginId)
    application
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.shadowJar)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    implementation(projects.core)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.okio)
}

var osClassifier: String
var fileExtension: String

when {
    OperatingSystem.current().isLinux -> {
        osClassifier = "linux-x86_64"
        fileExtension = "" // No extension for Linux
    }
    OperatingSystem.current().isWindows -> {
        osClassifier = "windows-x86_64"
        fileExtension = ".exe" // Windows needs .exe
    }
    OperatingSystem.current().isMacOsX -> {
        osClassifier = "macos-aarch64"
        fileExtension = "" // No extension for macOS
    }
    else -> throw GradleException("Unsupported OS: ${OperatingSystem.current()}")
}

graalvmNative {
    binaries {
        named("main") {
            mainClass = "org.timemates.rrpc.generator.kotlin.MainKt"
            buildArgs.addAll(
                "--initialize-at-build-time=kotlin.DeprecationLevel",
                "-H:ReflectionConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/reflect-config.json")}",
                "-H:ResourceConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/resource-config.json")}",
                "-H:Name=rrpc-kotlin-gen-$osClassifier"
            )
            useFatJar = true
        }
    }
}

val nativeBinary = providers.provider {
    layout.buildDirectory.file("native/nativeCompile/rrpc-kotlin-gen-${osClassifier}").get().asFile
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "kotlin-generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Kotlin Code Generator")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}

application {
    mainClass.set("org.timemates.rrpc.generator.kotlin.MainKt")
}

tasks.shadowJar {
    archiveClassifier.set("") // Ensures the main artifact is the shadow JAR
}

tasks.matching { it.name.startsWith("publish") }.configureEach {
    dependsOn("shadowJar")
}