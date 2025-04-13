package app.timemate.rrpc.gradle.configuration.type

public enum class PluginDependencyType {
    JAR,

    /**
     * Auto-assigns the necessary classifier for the dependency, depending
     * on the running systems. Use it if plugin is published in form of binary executables.
     */
    BINARY,
}