package app.timemate.rrpc.gradle.configuration.type

import org.gradle.api.tasks.Internal
import java.io.File
import java.nio.file.Path

/**
 * Represents a Proto input source. This can be a directory, an archive, or an external dependency.
 */
public sealed interface ProtoInput {
    public val type: ProtoDependencyType
    @get:Internal
    public val filesFilter: (File) -> Boolean

    /**
     * A directory-based Proto input.
     *
     * @property directory The path to the directory containing Proto files.
     */
    public data class Directory(
        public val directory: Path,
        override val type: ProtoDependencyType,
        @get:Internal
        override val filesFilter: (File) -> Boolean,
    ) : ProtoInput

    /**
     * Represents an archive-based Proto input, which can either be a file or a dependency.
     */
    public data class Artifact(
        public val dependency: Any,
        public override val type: ProtoDependencyType,
        @get:Internal
        override val filesFilter: (File) -> Boolean,
    ) : ProtoInput
}