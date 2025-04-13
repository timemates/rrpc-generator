package app.timemate.rrpc.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import app.timemate.rrpc.gradle.configuration.type.ProtoDependencyType
import app.timemate.rrpc.gradle.configuration.type.ProtoInput
import java.io.File

/**
 * Custom task to unpack proto files from given dependencies and directories.
 */
public abstract class UnpackProtoDependenciesTask : DefaultTask() {

    init {
        group = "rrpc"
    }

    @get:InputFiles
    internal abstract val inputFolders: ConfigurableFileCollection

    @get:Internal
    internal abstract val inputs: ListProperty<ProtoInput>

    // To be used indirectly in generation; generated upon usage
    // Separated in different directory after unpacking
    @get:InputFiles
    internal abstract val contextDependencies: ConfigurableFileCollection

    // To be used directly in generation, separated in different directory after unpacking
    @get:InputFiles
    internal abstract val sourceDependencies: ConfigurableFileCollection

    @get:OutputDirectory
    public abstract val dependenciesOutputDirectory: DirectoryProperty

    /**
     * Processes a collection of files, copying proto files using the provided filter.
     */
    private fun processFileCollection(
        files: Iterable<File>,
        dest: File,
        filter: (File) -> Boolean = { true },
    ) {
        files.forEach { file ->
            copyProtoFiles(file, dest, filter)
        }
    }

    @TaskAction
    public fun run() {
        val srcOut = dependenciesOutputDirectory.get().asFile.resolve("source")
        val ctxOut = dependenciesOutputDirectory.get().asFile.resolve("context")

        if (srcOut.exists()) {
            logger.lifecycle("Clearing previously extracted source files.")
            srcOut.deleteRecursively()
        }

        if (ctxOut.exists()) {
            logger.lifecycle("Clearing previously extracted context files.")
            ctxOut.deleteRecursively()
        }

        // --- Process the custom ProtoInput list
        inputs.get().forEach { protoInput ->
            when (protoInput) {
                is ProtoInput.Directory -> {
                    // For a directory input, convert the Path to a File and copy proto files.
                    val sourceDir = protoInput.directory.toFile()
                    // Determine the target directory based on a dependency type.
                    // If the type is SOURCE, then even transitively, we copy it into the context folder.
                    val targetDir = if (protoInput.type == ProtoDependencyType.SOURCE) srcOut else ctxOut
                    copyProtoFiles(sourceDir, targetDir, protoInput.filesFilter)
                }

                is ProtoInput.Artifact -> {
                    // For Artifact type, check the dependency and resolve to a FileCollection.
                    val dep = protoInput.dependency
                    val files: Iterable<File> = when (dep) {
                        is FileCollectionDependency -> {
                            dep.files
                        }

                        is ModuleDependency -> {
                            // Use the proper configuration based on the dependency type.
                            val configuration = when (protoInput.type) {
                                ProtoDependencyType.SOURCE -> project.configurations.findByName("rrpcContextProto")
                                else -> project.configurations.findByName("rrpcSourceProto")
                            }
                            configuration?.files ?: project.files(dep).files
                        }

                        else -> {
                            project.files(dep).files
                        }
                    }

                    // Route to context if type is SOURCE, otherwise source.
                    val targetDir = if (protoInput.type == ProtoDependencyType.SOURCE) srcOut else ctxOut
                    processFileCollection(files, targetDir, protoInput.filesFilter)
                }
            }
        }

        logger.lifecycle(
            "Proto dependencies unpacked to source folder: ${srcOut.absolutePath} and " +
                "context folder: ${ctxOut.absolutePath}"
        )
    }

    /**
     * Copies proto files from either a directory or an archive (jar/zip).
     *
     * @param source The file or folder to copy from.
     * @param dest The destination folder.
     * @param filter A filtering function to check files.
     */
    private fun copyProtoFiles(source: File, dest: File, filter: (File) -> Boolean) {
        if (source.isDirectory) {
            project.copy { ->
                from(source) {
                    eachFile {
                        if (!filter(file)) {
                            exclude()
                        }
                    }
                }

                into(dest)
            }
        } else {
            if (source.extension !in listOf("jar", "zip"))
                return project.logger.warn("File ${source.path} cannot be used as a dependency, ignoring.")

            project.copy {
                from(project.zipTree(source)) {
                    include("proto/**/*.proto")
                    eachFile {
                        if (!filter(file)) {
                            exclude()
                        }
                    }
                }
                into(dest)
            }
        }
    }
}
