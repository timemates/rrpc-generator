package org.timemates.rrpc.codegen.option

import org.timemates.rrpc.codegen.annotation.ExperimentalGeneratorFunctionality

/**
 * Defines how extensions should be handled in generated code,
 * affecting their accessibility and placement in different languages.
 *
 * This setting applies to all extension declarations, whether they are
 * at the top level or inside a message. The default behavior differs based on
 * placement:
 * - **Top-level `extend`**: Defaults to [EXTENSION], meaning options
 *   are treated as global extensions.
 * - **Nested `extend` inside a message**: Defaults to [REGULAR], meaning
 *   options are accessed through the enclosing message instead of the global scope.
 *
 * This setting is particularly relevant for languages with extension support
 * (e.g., Kotlin, Swift, C#), where options can be generated as member functions
 * rather than global extensions.
 */
@ExperimentalGeneratorFunctionality
public enum class ExtensionGenerationStrategy {
    /**
     * Uses a namespaced approach where options are accessed through
     * the enclosing message instead of the global extension scope.
     *
     * Example:
     * - **Nested Extend**: `MessageClassWithExtendInside.myOption`
     * - **Top-Level Extend**: Can be generated as a regular field instead of an extension.
     *
     * This approach is recommended for languages that support extensions,
     * such as Kotlin, Swift, and C#.
     */
    REGULAR,

    /**
     * Treats extensions as globally accessible regardless of where they are declared.
     *
     * Example: `RSOption.myOption`, even if the extend is inside a message.
     *
     * **Special case for Kotlin**: Options are still generated inside
     * the companion object of the defining message but can be imported like
     * regular top-level extensions.
     */
    EXTENSION,
}