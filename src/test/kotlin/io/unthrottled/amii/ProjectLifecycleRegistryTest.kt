package io.unthrottled.amii

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ProjectLifecycleRegistryTest {

  @Test
  fun `same project can only be opened once until closed`() {
    val registry = ProjectLifecycleRegistry()

    assertThat(registry.markProjectOpened("project-one", isDisposed = false)).isTrue
    assertThat(registry.markProjectOpened("project-one", isDisposed = false)).isFalse

    registry.markProjectClosed("project-one")

    assertThat(registry.markProjectOpened("project-one", isDisposed = false)).isTrue
  }

  @Test
  fun `disposed projects are never registered`() {
    val registry = ProjectLifecycleRegistry()

    assertThat(registry.markProjectOpened("project-one", isDisposed = true)).isFalse
    assertThat(registry.markProjectOpened("project-one", isDisposed = false)).isTrue
  }
}
