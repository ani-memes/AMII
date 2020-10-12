package com.github.unthrottled.amii.services

import com.intellij.openapi.project.Project
import com.github.unthrottled.amii.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
