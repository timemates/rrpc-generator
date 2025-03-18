package org.timemates.rrpc.generator.kotlin

import okio.buffer
import okio.sink
import okio.source
import org.timemates.rrpc.codegen.plugin.PluginService

public suspend fun main(args: Array<String>): Unit {
    PluginService.main(
        args = args.asList(),
        input = System.`in`.source().buffer(),
        output = System.out.sink().buffer(),
        service = KotlinPluginService,
    )
}