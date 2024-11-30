package org.timemates.rrpc.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.timemates.rrpc.gradle.RRpcExtension

public abstract class GenerateRRpcCodeTask : DefaultTask() {
    init {
        group = "rrpc"
        description = "Generates RRpc code based on configured plugins and inputs."
    }

    @TaskAction
    public fun generate() {
        val extension = project.extensions.findByType(RRpcExtension::class.java)
            ?: throw GradleException("RRpcExtension is not configured")
        
//        val outputDir = project.layout.buildDirectory.dir("rrpc/generated").get().asFile
//        outputDir.mkdirs()

        logger.lifecycle("Generating RRpc code in $outputDir")
        // Call your generation logic here, for example:
        // 1. Resolve inputs
        // 2. Call the plugins to generate files
    }
}