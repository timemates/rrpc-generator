package org.timemates.rrpc.generator.cli

import org.timemates.rrpc.codegen.plugin.GeneratorCommunication
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor

data class Plugin(
    val name: String,
    val description: String,
    val options: List<OptionDescriptor>,
    val communication: GeneratorCommunication,
    val process: Process,
)