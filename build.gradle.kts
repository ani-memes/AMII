import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.6.0"
  // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
  id("org.jetbrains.intellij") version "1.6.0"
  // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
  id("org.jetbrains.changelog") version "1.1.2"
  // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
  id("io.gitlab.arturbosch.detekt") version "1.16.0"
  // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project
val idePath: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}
dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
  implementation("io.sentry:sentry:6.0.0")
  testImplementation("org.assertj:assertj-core:3.23.1")
  testImplementation("io.mockk:mockk:1.12.4")
}

configurations {
  implementation.configure {
    // sentry brings in a slf4j that breaks when
    // with the platform slf4j
    exclude("org.slf4j")
  }
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
  pluginName.set(pluginName_)
  version.set(platformVersion)
  type.set(platformType)
  downloadSources.set(platformDownloadSources.toBoolean())
  updateSinceUntilBuild.set(true)

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  plugins.set(
    platformPlugins.split(',')
      .map(String::trim)
      .filter(String::isNotEmpty)
  )
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
  config = files("./detekt-config.yml")
  buildUponDefaultConfig = true
  autoCorrect = true

  reports {
    html.enabled = false
    xml.enabled = false
    txt.enabled = false
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
  }
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
  }

  withType<Detekt> {
    jvmTarget = "11"
  }

  runIde {
    maxHeapSize = "2g"
    autoReloadPlugins.set(false)
    val idePath = properties("idePath")
    if (idePath.isNotEmpty()) {
      ideDir.set(file(idePath))
    }
  }

  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set(pluginVersion)
    sinceBuild.set(pluginSinceBuild)
    untilBuild.set(pluginUntilBuild)

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(
      provider {
        File("${project.projectDir}/README.md").readText().lines().run {
          val start = "<!-- Plugin description -->"
          val end = "<!-- Plugin description end -->"

          if (!containsAll(listOf(start, end))) {
            throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
          }
          subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }
      }
    )

    changeNotes.set(
      provider {
        markdownToHTML(File("${project.projectDir}/docs/RELEASE-NOTES.md").readText())
      }
    )
  }

  runPluginVerifier {
    failureLevel.set(listOf(FailureLevel.COMPATIBILITY_PROBLEMS))
    ideVersions.set(pluginVerifierIdeVersions.split(",").filter { it.isNotEmpty() })
  }
}
