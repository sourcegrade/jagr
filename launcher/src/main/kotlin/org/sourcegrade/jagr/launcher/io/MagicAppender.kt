package org.sourcegrade.jagr.launcher.io

import org.apache.logging.log4j.core.*
import org.sourcegrade.jagr.launcher.env.Environment.stdOut
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import org.sourcegrade.jagr.launcher.env.Environment
import java.io.PrintStream
import org.sourcegrade.jagr.launcher.io.MagicAppender
import java.io.Serializable
import java.nio.charset.StandardCharsets

@Plugin(
  name = "MagicAppender",
  category = Core.CATEGORY_NAME,
  elementType = Appender.ELEMENT_TYPE,
  printObject = true
)
class MagicAppender private constructor(
  name: String,
  filter: Filter?,
  layout: Layout<out Serializable?>?,
  ignoreExceptions: Boolean,
  properties: Array<Property>?
) : AbstractAppender(name, filter, layout, ignoreExceptions, properties) {

  override fun append(event: LogEvent) {
    val out = stdOut
    out.write(event.level.intLevel() / 100)
    System.err.println(event.level.intLevel().toString() + event.message.formattedMessage)
    val l = event.message.formattedMessage.toByteArray(StandardCharsets.UTF_8).size
    out.write(l shr 24)
    out.write(l shr 16)
    out.write(l shr 8)
    out.write(l)
    out.write(event.message.formattedMessage.toByteArray(StandardCharsets.UTF_8))
  }

  companion object {
    @JvmStatic
    @PluginFactory
    fun createAppender(
      @PluginAttribute("name") name: String,
      @PluginElement("Layout") layout: Layout<out Serializable?>?,
      @PluginElement("Filter") filter: Filter?): MagicAppender {
      System.err.println("INIT APPENDER")
      return MagicAppender(name, filter, PatternLayout.createDefaultLayout(), true, null)
    }
  }
}
