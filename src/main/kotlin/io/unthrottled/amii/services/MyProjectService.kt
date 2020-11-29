package io.unthrottled.amii.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.unthrottled.amii.tools.PluginMessageBundle

// todo: remove me
class MyProjectService(private val project: Project) : Disposable {

  init {
    println(getMessage())
  }

  fun getMessage() = PluginMessageBundle.message("projectService", project.name)
  override fun dispose() {
  }
}
