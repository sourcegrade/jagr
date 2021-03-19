package org.jagrkt.common

import com.google.inject.Guice

fun main(vararg args: String) {
  println("Loading JagrKt...")
  val injector = Guice.createInjector(JagrKtModule())
  injector.getInstance(org.jagrkt.common.JagrKtImpl::class.java).run()
}
