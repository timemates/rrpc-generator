package org.timemates.rrpc.gradle.configuration

import org.gradle.api.provider.ListProperty
import org.timemates.rrpc.gradle.type.ProtoInput
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
public class ProtoInputBuilder(private val list: ListProperty<ProtoInput>) {

    /**
     * Adds a directory to the list of Proto input sources.
     *
     * @param file A file representing the directory to be included as a Proto input source.
     */
    public fun directory(file: File) {
        list.add(ProtoInput.Directory(file.toPath()))
    }

    /**
     * Adds a directory to the list of Proto input sources.
     *
     * @param path A path representing the directory to be included as a Proto input source.
     */
    public fun directory(path: Path) {
        list.add(ProtoInput.Directory(path))
    }

    /**
     * Adds an archive file to the list of Proto input sources.
     *
     * @param file A file representing the archive to be included as a Proto input source.
     * @param includes A list of paths within the resources folder of JAR/klib, which will be
     * added to the inputs. By default, it includes all .proto files from the resources folder.
     */
    public fun artifact(
        file: File,
        includes: List<String> = listOf("/"),
        excludes: List<String> = emptyList(),
    ) {
        list.add(ProtoInput.Artifact.File(file.toPath(), includes, excludes))
    }

    /**
     * Adds an archive file to the list of Proto input sources.
     *
     * @param path A path representing the archive to be included as a Proto input source.
     * @param includes A list of paths within the resources folder of JAR/klib, which will be
     * added to the inputs. By default, it includes all .proto files from the resources folder.
     */
    public fun artifact(
        path: Path,
        includes: List<String> = listOf("/"),
        excludes: List<String> = emptyList(),
    ) {
        list.add(ProtoInput.Artifact.File(path, includes, excludes))
    }

    /**
     * Adds an external dependency (e.g., a Maven artifact) as a Proto input source.
     *
     * @param coordinates The Maven coordinates of the external dependency.
     * @param includes A list of paths within the resources folder of JAR/klib, which will be
     * added to the inputs. By default, it includes all .proto files from the resources folder.
     */
    public fun external(
        coordinates: String,
        includes: List<String> = listOf("/"),
        excludes: List<String> = emptyList(),
    ) {
        list.add(ProtoInput.Artifact.Dependency(coordinates, includes, excludes))
    }
}