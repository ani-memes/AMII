package io.unthrottled.amii.services

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import org.assertj.core.api.Assertions
import org.junit.Test

class MyProjectServiceTest : LightPlatformCodeInsightFixture4TestCase() {

  @Test
  fun getMessage() {
    Assertions.assertThat(
      project.getService(MyProjectService::class.java)
        .getMessage()
    ).startsWith("Project service: light_temp_")
  }
}
