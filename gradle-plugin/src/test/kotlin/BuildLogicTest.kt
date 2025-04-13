import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildLogicTest {
    @TempDir
    lateinit var testProjectDir: File
    private val settingsFile: File by lazy {
        testProjectDir.resolve("settings.gradle.kts")
    }
    private val buildFile: File by lazy {
        testProjectDir.resolve("build.gradle.kts")
    }

    @BeforeEach
    fun setup() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
    }

    @Test
    fun `test whether kotlin plugin is applied`() {
        val buildFileContent = """
            import app.timemate.rrpc.gradle.configuration.type.PluginDependencyType
            
            plugins {
                // no need to apply kotlin plugin for this test
                java
                id("app.timemate.rrpc")
            }
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            rrpc {
                plugins {
                    add(
                        notation = "app.timemate.rrpc:kotlin-generator:SNAPSHOT",
                        type = PluginDependencyType.JAR,
                    ) {
                        option("server_generation", false)
                    }
                }
            }
        """.trimIndent()

        buildFile.writeText(buildFileContent)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("rrpcGeneratorHelp", "--stacktrace")
            .withDebug(false)
            .withPluginClasspath()
            .build()

        val taskResult = result.task(":rrpcGeneratorHelp")
        assert(taskResult != null) { "Task is not found." }
        assert(result.output.contains("▶ Plugin: rrpc-kotlin-gen")) {
            "Kotlin code-generation plugin was not loaded."
        }
        assert(taskResult!!.outcome == TaskOutcome.SUCCESS) {
            "Task result is not successful, result: ${taskResult.outcome}."
        }
    }
}