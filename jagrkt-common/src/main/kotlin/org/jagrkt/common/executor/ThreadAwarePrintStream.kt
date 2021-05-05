package org.jagrkt.common.executor

import java.io.OutputStream
import java.io.PrintStream
import java.util.Locale

const val GRADING_THREAD_PREFIX = "G_Thread-"

class ThreadAwarePrintStream(delegate: OutputStream) : PrintStream(delegate) {
  private inline fun <T> check(default: T, block: () -> T): T =
    if (!Thread.currentThread().name.startsWith(GRADING_THREAD_PREFIX)) block() else default

  override fun print(b: Boolean) = check(Unit) { super.print(b) }
  override fun print(c: Char) = check(Unit) { super.print(c) }
  override fun print(i: Int) = check(Unit) { super.print(i) }
  override fun print(l: Long) = check(Unit) { super.print(l) }
  override fun print(f: Float) = check(Unit) { super.print(f) }
  override fun print(d: Double) = check(Unit) { super.print(d) }
  override fun print(s: CharArray) = check(Unit) { super.print(s) }
  override fun print(s: String?) = check(Unit) { super.print(s) }
  override fun print(obj: Any?) = check(Unit) { super.print(obj) }
  override fun println() = check(Unit) { super.println() }
  override fun println(x: Boolean) = check(Unit) { super.println(x) }
  override fun println(x: Char) = check(Unit) { super.println(x) }
  override fun println(x: Int) = check(Unit) { super.println(x) }
  override fun println(x: Long) = check(Unit) { super.println(x) }
  override fun println(x: Float) = check(Unit) { super.println(x) }
  override fun println(x: Double) = check(Unit) { super.println(x) }
  override fun println(x: CharArray) = check(Unit) { super.println(x) }
  override fun println(x: String?) = check(Unit) { super.println(x) }
  override fun println(x: Any?) = check(Unit) { super.println(x) }
  override fun printf(format: String, vararg args: Any?): PrintStream =
    check(this) { super.printf(format, *args) }

  override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream =
    check(this) { super.printf(l, format, *args) }
}
