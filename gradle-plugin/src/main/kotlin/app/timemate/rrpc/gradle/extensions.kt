package app.timemate.rrpc.gradle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLauncher
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.generator.plugin.loader.ProcessPluginService
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal suspend fun List<File>.loadAsPlugins(
    logger: RLogger,
    launcher: JavaLauncher,
): List<ProcessPluginService> =
    coroutineScope {
        mapNotNull { file ->
            async(Dispatchers.IO) {
                val commands = if (file.extension == "jar")
                    listOf(launcher.executablePath.asFile.absolutePath, "-jar", file.absolutePath)
                else listOf(file.absolutePath)

                ProcessPluginService.load(commands, logger)
            }
        }.awaitAll()
    }

internal fun Any?.toStringValueRepresentative(): String {
    return when (this) {
        is File -> absolutePath
        is Path -> absolutePathString()
        is FileSystemLocation -> asFile.absolutePath
        is Provider<*> -> get().toStringValueRepresentative()
        else -> this.toString()
    }
}

internal fun Dependency.collectArtifactFiles(configuration: Configuration): List<File> {
    return when (this) {
        is ProjectDependency -> listOf(
            configuration.resolvedConfiguration.resolvedArtifacts.firstOrNull {
                val id =
                    it.id.componentIdentifier as? ProjectComponentIdentifier ?: return@firstOrNull false
                id.projectPath == this.path
            }?.file ?: error("Unable to find dependency: $this.")
        )

        is ModuleDependency -> listOf(
            configuration.resolvedConfiguration.resolvedArtifacts.firstOrNull {
                val id = it.id.componentIdentifier as? ModuleComponentIdentifier ?: return@firstOrNull false
                id.moduleIdentifier.name == this.name && id.moduleIdentifier.group == this.group
            }?.file ?: error("Unable to find dependency: $this.")
        )

        is FileCollectionDependency -> files.toList()
        else -> error("Unsupported dependency as a plugin $this.")
    }
}