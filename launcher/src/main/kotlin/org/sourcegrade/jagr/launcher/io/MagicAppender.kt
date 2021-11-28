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
import org.sourcegrade.jagr.launcher.executor.ProcessWorker
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
    out.write(ProcessWorker.MARK_LOG_MESSAGE_BYTE)
    out.write(event.level.intLevel() / 100)
    val msg = event.message.formattedMessage.toByteArray(StandardCharsets.UTF_8)
    val msgLength = msg.size
    out.write(msgLength shr 24)
    out.write(msgLength shr 16)
    out.write(msgLength shr 8)
    out.write(msgLength)
    out.write(msg)
  }

  companion object {
    @JvmStatic
    @PluginFactory
    fun createAppender(
      @PluginAttribute("name") name: String,
      @PluginElement("Layout") layout: Layout<out Serializable?>?,
      @PluginElement("Filter") filter: Filter?): MagicAppender {
      return MagicAppender(name, filter, layout ?: PatternLayout.createDefaultLayout(), true, null)
    }
  }
}
