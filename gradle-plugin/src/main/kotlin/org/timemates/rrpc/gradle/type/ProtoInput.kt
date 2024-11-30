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
    public sealed interface Artifact : ProtoInput {

        /** A list of includes for the archive's resources directory. Defaults to all files ("/"). */
        public val includes: List<String>

        /** A list of excludes for the archive's resource directory. Empty by default. */
        public val excludes: List<String>

        /**
         * Represents an archive file containing Proto files.
         *
         * @property file The path to the archive file.
         * @property includes A list of file patterns to include from the archive. Defaults to all files ("/").
         * @property excludes A list of excludes for the archive's resource directory. Empty by default.
         */
        public data class File(
            val file: Path,
            override val includes: List<String> = listOf("/"),
            override val excludes: List<String> = emptyList(),
        ) : Artifact

        /**
         * Represents an external archive dependency (e.g., a Maven dependency).
         *
         * @property coordinates The Maven coordinates of the external dependency.
         * @property includes A list of file patterns to include from the archive. Defaults to all files ("/").
         * @property excludes A list of excludes for the archive's resource directory. Empty by default.
         */
        public data class Dependency(
            val coordinates: String,
            override val includes: List<String> = listOf("/"),
            override val excludes: List<String> = emptyList(),
        ) : Artifact
    }
}