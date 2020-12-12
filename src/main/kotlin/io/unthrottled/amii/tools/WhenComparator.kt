package io.unthrottled.amii.tools

interface WhenComparator<T> {
  fun test(other: T): Boolean
  operator fun contains(other: T) = test(other)
}

fun <T : Comparable<T>> lt(value: T) = object : WhenComparator<T> {
  override fun test(other: T) = other < value
}
