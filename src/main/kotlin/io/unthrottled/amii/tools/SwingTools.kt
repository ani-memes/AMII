package io.unthrottled.amii.tools

import java.awt.EventQueue

fun assertNotAWTThread() {
  if (EventQueue.isDispatchThread()) {
    error("You are on the AWT thread, check yourself before you wreck yourself")
  }
}
