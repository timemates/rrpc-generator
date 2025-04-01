package org.timemates.rrpc.codegen.plugin

import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.logging.RLogger
import org.timemates.rrpc.codegen.schema.RSFile

/**
 * Type of plugin that is used to generate a code.
 */
public interface GenerationPluginService : PluginService {
    /**
     * Generates code based on the provided input files.
     *
     * This function is triggered when the generator sends a `SendInput` signal containing
     * a list of files (`RSFile`) to process. The plugin is responsible for handling these
     * files and returning a signal indicating the status of the request.
     *
     * @param files The list of files to be processed by the plugin.
     */
    public suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
        logger: RLogger,
    )
}