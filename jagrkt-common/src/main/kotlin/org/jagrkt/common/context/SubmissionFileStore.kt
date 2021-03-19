package org.jagrkt.common.context

import org.jagrkt.api.testing.Submission
import java.nio.file.FileStore
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.FileStoreAttributeView

class SubmissionFileStore(
  private val submission: Submission,
) : FileStore() {
  override fun name(): String = "FileSystem-${submission.info}"
  override fun type(): String = "submission"
  override fun isReadOnly(): Boolean = true
  override fun getTotalSpace(): Long = 0 // TODO: implement?
  override fun getUsableSpace(): Long = 0
  override fun getUnallocatedSpace(): Long = 0
  override fun supportsFileAttributeView(type: Class<out FileAttributeView>?): Boolean = false
  override fun supportsFileAttributeView(name: String?): Boolean = false
  override fun <V : FileStoreAttributeView?> getFileStoreAttributeView(type: Class<V>?): V {
    TODO("Not yet implemented")
  }

  override fun getAttribute(attribute: String?): Any {
    TODO("Not yet implemented")
  }
}
