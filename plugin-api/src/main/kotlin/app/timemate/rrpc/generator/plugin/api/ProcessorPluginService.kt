package app.timemate.rrpc.generator.plugin.api

import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.proto.schema.RSFile

/**
 * Type of plugin that is used for modifying the input files.
 */
public interface ProcessorPluginService : PluginService {

    /**
     * Modifies the input of user's files.
     *
     * The example of such a plugin can be a plugin that filters entities,
     * according to the requirements, such as versioning.
     */
    public suspend fun process(
        options: GenerationOptions,
        files: List<RSFile>,
        logger: RLogger,
    ): List<RSFile>
}