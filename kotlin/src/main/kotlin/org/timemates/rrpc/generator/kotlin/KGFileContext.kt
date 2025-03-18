package org.timemates.rrpc.generator.kotlin

import com.squareup.kotlinpoet.FileSpec
import org.timemates.rrpc.codegen.schema.RSElementLocation
import org.timemates.rrpc.generator.kotlin.adapter.internal.ImportRequirement

public class KGFileContext(
    private val fileSpec: FileSpec.Builder,
    public val location: RSElementLocation,
) {
    /**
     * Adds import to the file where generation takes place.
     */
    public fun addImport(import: ImportRequirement) {
        fileSpec.addImport(import.packageName.value, import.simpleNames)
    }
}