package org.timemates.rrpc.codegen.plugin

import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.logging.RLogger
import org.timemates.rrpc.codegen.schema.RSFile

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