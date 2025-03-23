package org.timemates.rrpc.gradle.configuration

import org.timemates.rrpc.gradle.configuration.type.ProtoDependencyType
import org.timemates.rrpc.gradle.configuration.type.ProtoInput
import java.io.File
import java.nio.file.Path

/**
 * Scope for managing and configuring Proto input sources.
 *
 * This class provides methods to define different types of Proto input sources that can be
 * used in the build process, including directories, archive files, and external dependencies.
 *
 * @property list A list of `ProtoInput` objects which can be directories or archives to be used as Proto input sources.
 */
public class ProtoInputBuilder(private val base: File, private val onAdded: (ProtoInput, ProtoDependencyType) -> Unit) {

    /**
     * This type of dependency is used only to resolve types. It's useful
     *  if the artifact you're using already has generated code, and you want to use it.
     */
    public fun context(block: SelectBuilder.() -> Unit) {
        SelectBuilder(base) { protoInput -> onAdded(protoInput, ProtoDependencyType.CONTEXT) }.apply(block)
    }

    /**
     * This type of dependency is used to generate code.
     */
    public fun source(block: SelectBuilder.() -> Unit) {
        SelectBuilder(base) { protoInput -> onAdded(protoInput, ProtoDependencyType.SOURCE) }.apply(block)
    }

    public class SelectBuilder(private val base: File, private val onAdded: (ProtoInput) -> Unit, ) {
        /**
         * Adds a directory to the list of Proto input sources/context.
         *
         * @param file A file representing the directory to be included as a Proto input source.
         */
        public fun directory(file: File) {
            if (!file.exists()) throw IllegalArgumentException("Directory does not exist at ${file.absolutePath}, but expected to.")
            if (!file.isDirectory) throw IllegalArgumentException("Expected a directory, but got file at ${file.absolutePath}")
            onAdded(ProtoInput.Directory(file.toPath()))
        }

        /**
         * Adds a directory to the list of Proto input sources/context.
         *
         * @param path A path representing the directory to be included as a Proto input source.
         */
        public fun directory(path: Path) {
            onAdded(ProtoInput.Directory(path))
        }

        /**
         * Adds a directory to the list of proto input sources/context.
         */
        public fun directory(path: String) {
            onAdded(ProtoInput.Directory(File(base, path).toPath()))
        }

        /**
         * Adds an .jar archive file to the list of Proto input sources.
         *
         * @param notation A path representing the archive to be included as a Proto input source.
         * @param includes A list of paths within the resources folder of JAR/klib, which will be
         * added to the inputs. By default, it includes all .proto files from the resources folder.
         */
        public fun artifact(
            notation: Any,
            includes: List<String> = listOf("/"),
            excludes: List<String> = emptyList(),
        ) {
            onAdded(ProtoInput.Artifact(notation, includes, excludes))
        }
    }
}