package io.unthrottled.amii.services

import com.intellij.openapi.project.Project
import io.unthrottled.amii.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
