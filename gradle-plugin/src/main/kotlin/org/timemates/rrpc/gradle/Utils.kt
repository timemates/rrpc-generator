package org.timemates.rrpc.gradle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal suspend fun Configuration.loadAsPlugins(): List<ProcessPluginService> = coroutineScope {
    resolvedConfiguration.resolvedArtifacts.map { artifact ->
        async(Dispatchers.IO) {
            if (!artifact.file.canExecute())
                artifact.file.setExecutable(true)

            ProcessPluginService.load(
                listOf(artifact.file.absolutePath),
            )
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