package io.unthrottled.amii.listeners

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project
import io.unthrottled.amii.services.ConsoleFilterFactory
import io.unthrottled.amii.tools.Logging

class ConsoleListener : ConsoleFilterProvider, Logging {

  override fun getDefaultFilters(project: Project): Array<Filter> =
    project.getService(ConsoleFilterFactory::class.java).getFilter()
      .map { arrayOf(it) }
      .orElse(Filter.EMPTY_ARRAY)
}
