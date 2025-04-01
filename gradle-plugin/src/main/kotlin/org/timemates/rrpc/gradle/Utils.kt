package org.timemates.rrpc.gradle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLauncher
import org.timemates.rrpc.codegen.logging.RLogger
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal suspend fun List<ResolvedArtifact>.loadAsPlugins(
    logger: RLogger,
    launcher: JavaLauncher,
    get: Map<ModuleIdentifier, Map<String, Any>>
): List<ProcessPluginService> =
    coroutineScope {
        mapNotNull { artifact ->
            async(Dispatchers.IO) {
                val commands = if (artifact.extension == "jar")
                    listOf(launcher.executablePath.asFile.absolutePath, "-jar", artifact.file.absolutePath)
                else listOf(artifact.file.absolutePath)

                ProcessPluginService.load(commands, logger)
            }
        }.awaitAll()
    }

internal fun Any?.toStringValueRepresentative(): String? = when (this) {
    is File -> absolutePath
    is Path -> absolutePathString()
    is FileSystemLocation -> asFile.absolutePath
    is Provider<*> -> (get() as? Any)?.toStringValueRepresentative()
    else -> this?.toString()
}