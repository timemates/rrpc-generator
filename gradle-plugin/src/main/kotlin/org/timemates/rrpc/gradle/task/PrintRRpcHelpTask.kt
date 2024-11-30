package org.timemates.rrpc.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public abstract class PrintRRpcHelpTask : DefaultTask() {
    init {
        group = "rrpc"
        description = "Prints help information about RRpc Gradle Plugin."
    }

    @TaskAction
    public fun printHelp() {
        val helpMessage = """
            RRpc Plugin Help:
            - configure the `rrpc` block in your build.gradle(.kts)
            - Use the following tasks:
                - generateRRpcCode: Generate source code.
                - printRRpcHelp: Print help for using this plugin.
        """.trimIndent()

        logger.lifecycle(helpMessage)
    }
}