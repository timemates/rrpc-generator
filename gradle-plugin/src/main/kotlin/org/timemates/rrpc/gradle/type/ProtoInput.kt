package org.timemates.rrpc.gradle.type

import java.nio.file.Path

/**
 * Represents a Proto input source. This can be a directory, an archive, or an external dependency.
 */
public sealed interface ProtoInput {

    /**
     * A directory-based Proto input.
     *
     * @property directory The path to the directory containing Proto files.
     */
    public data class Directory(val directory: Path) : ProtoInput

    /**
     * Represents an archive-based Proto input, which can either be a file or a dependency.
     */
    public class Artifact(
        public val notation: Any,
        public val includes: List<String> = listOf("/"),
        public val excludes: List<String> = emptyList(),
    ) : ProtoInput
}