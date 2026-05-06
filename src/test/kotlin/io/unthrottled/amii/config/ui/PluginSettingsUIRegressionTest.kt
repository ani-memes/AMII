package io.unthrottled.amii.config.ui

import io.unthrottled.amii.events.UserEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PluginSettingsUIRegressionTest {

  @Test
  fun `parse exit codes ignores invalid and empty values`() {
    val exitCodes = PluginSettingsUI.parseExitCodes("0,,abc, 1 ,2147483648,-9")

    assertThat(exitCodes).containsExactly(-9, 0, 1)
  }

  @Test
  fun `parse exit codes removes duplicates and sorts`() {
    val exitCodes = PluginSettingsUI.parseExitCodes("2,1,2,0,-1")

    assertThat(exitCodes).containsExactly(-1, 0, 1, 2)
  }

  @Test
  fun `bitmask update clears only the requested bit`() {
    val initialValue = UserEvents.STARTUP.value or UserEvents.TEST.value or UserEvents.TASK.value

    val result = PluginSettingsUI.updateBitmask(initialValue, UserEvents.TEST.value, false)

    assertThat(result and UserEvents.STARTUP.value).isEqualTo(UserEvents.STARTUP.value)
    assertThat(result and UserEvents.TEST.value).isZero()
    assertThat(result and UserEvents.TASK.value).isEqualTo(UserEvents.TASK.value)
  }

  @Test
  fun `bitmask update setting an already set bit is stable`() {
    val initialValue = UserEvents.STARTUP.value or UserEvents.TEST.value

    val result = PluginSettingsUI.updateBitmask(initialValue, UserEvents.TEST.value, true)

    assertThat(result).isEqualTo(initialValue)
  }

  @Test
  fun `bitmask update clearing an already clear bit is stable`() {
    val initialValue = UserEvents.STARTUP.value

    val result = PluginSettingsUI.updateBitmask(initialValue, UserEvents.TEST.value, false)

    assertThat(result).isEqualTo(initialValue)
  }
}
