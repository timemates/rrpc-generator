package app.timemate.rrpc.gradle.configuration.type

public enum class ProtoDependencyType {
    /**
     * This type of dependency is used to generate code.
     */
    SOURCE,

    /**
     * This type of dependency is used only to resolve types. It's useful
     *  if the artifact you're using already has generated code, and you want to use it.
     */
    CONTEXT,
}