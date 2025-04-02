package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import org.timemates.rrpc.codegen.annotation.ExperimentalGeneratorFunctionality
import org.timemates.rrpc.codegen.option.ExtensionGenerationStrategy
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import kotlin.jvm.JvmInline


@Serializable
@JvmInline
public value class RSOptions(
    public val list: List<RSOption>
) {
    public companion object {
        public val EMPTY: RSOptions = RSOptions(emptyList())

        public val FILE_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.FileOptions")
        public val MESSAGE_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.MessageOptions")
        public val SERVICE_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.ServiceOptions")
        public val FIELD_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.FieldOptions")
        public val ONEOF_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.OneofOptions")
        public val ENUM_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.EnumOptions")
        public val ENUM_VALUE_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.EnumValueOptions")
        public val METHOD_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.MethodOptions")
        public val EXTENSION_RANGE_OPTIONS: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.ExtensionRangeOptions")
    }

    public operator fun get(fieldUrl: RSTypeMemberUrl): RSOption? {
        return list.firstOrNull { it.fieldUrl == fieldUrl }
    }

    public operator fun contains(fieldUrl: RSTypeMemberUrl): Boolean {
        return get(fieldUrl) != null
    }

    public fun withFilteredFrom(collection: Collection<RSTypeMemberUrl>): RSOptions {
        return RSOptions(list.filterNot { it.fieldUrl in collection })
    }
}

public val RSOptions.isDeprecated: Boolean get() = (this[RSOption.DEPRECATED]?.value as? RSOption.Value.Raw)?.string?.toBooleanStrictOrNull() ?: false

public val RSOptions.sourceOnly: Boolean get() = (this[RSOption.RETENTION]?.value as? RSOption.Value.Raw)?.string == "RETENTION_SOURCE"
    || get(RSOption.SOURCE_ONLY_MESSAGE)?.value?.toString() == "true"
    || get(RSOption.SOURCE_ONLY_ENUM)?.value?.toString() == "true"

public val RSOptions.isRetentionRuntime: Boolean get() = (this[RSOption.RETENTION]?.value as? RSOption.Value.Raw)?.string != "RETENTION_SOURCE"

/**
 * The decision of how to generate is dependent on the context. Read the documentation of
 * [ExtensionGenerationStrategy] to learn more.
 */
@ExperimentalGeneratorFunctionality
public val RSOptions.extensionGenerationStrategy: ExtensionGenerationStrategy?
    get() = (this[RSOption.RETENTION]?.value as? RSOption.Value.Raw)?.string
        ?.let { ExtensionGenerationStrategy.valueOf(it) }