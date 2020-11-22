package io.unthrottled.amii.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.unthrottled.amii.tools.MyBundle

class MyProjectService(private val project: Project) : Disposable {

  init {
    println(getMessage())
  }

  fun getMessage() = MyBundle.message("projectService", project.name)
  override fun dispose() {
  }
}
