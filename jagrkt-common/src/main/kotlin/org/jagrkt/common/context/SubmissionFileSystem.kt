package org.jagrkt.common.context

import org.jagrkt.api.testing.Submission
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.nio.file.WatchService
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

class SubmissionFileSystem(
  private val submission: Submission,
) : FileSystem() {

  private val rootDirs = listOf(Paths.get("/"))
  private val fileStore = SubmissionFileStore(submission)

  override fun close() {}

  override fun provider(): FileSystemProvider {
    TODO("Not yet implemented")
  }

  override fun isOpen(): Boolean = true
  override fun isReadOnly(): Boolean = true
  override fun getSeparator(): String = "/"
  override fun getRootDirectories(): Iterable<Path> = rootDirs
  override fun getFileStores(): Iterable<FileStore> = listOf(fileStore)
  override fun supportedFileAttributeViews(): Set<String> = setOf()

  override fun getPath(first: String, vararg more: String?): Path {
    TODO("Not yet implemented")
  }

  override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher {
    TODO("Not yet implemented")
  }

  override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
    TODO("Not yet implemented")
  }

  override fun newWatchService(): WatchService {
    TODO("Not yet implemented")
  }
}
