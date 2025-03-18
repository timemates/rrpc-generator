package org.timemates.rrpc.generator.kotlin.adapter.internal

import org.timemates.rrpc.codegen.schema.value.RSPackageName

public data class ImportRequirement(
    public val packageName: RSPackageName,
    public val simpleNames: List<String> = emptyList(),
)