package io.unthrottled.amii.tools

import java.awt.EventQueue
import java.lang.IllegalStateException

fun assertNotAWTThread() {
  if (EventQueue.isDispatchThread()) {
    throw IllegalStateException("You are on the AWT thread, check yourself before you wreck yourself")
  }
}
