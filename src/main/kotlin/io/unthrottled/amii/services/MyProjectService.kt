package io.unthrottled.amii.services

import com.intellij.openapi.project.Project
import io.unthrottled.amii.MyBundle

class MyProjectService(private val project: Project) {

  init {
    println(getMessage())
  }

  fun getMessage() = MyBundle.message("projectService", project.name)
}
