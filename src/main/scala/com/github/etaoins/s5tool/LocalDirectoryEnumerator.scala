package com.github.etaoins.s5tool

import java.io.File

object LocalDirectoryEnumerator {
  private def crawlDirectory(relativeRoot : String, directory : File) : List[LocalDirectoryEntry] = {
    directory.listFiles.toList.flatMap { dirEntry => 
      dirEntry match {
        case file if dirEntry.isFile => 
          // Add this single file on
          LocalDirectoryEntry(
            siteRelativePath=relativeRoot + dirEntry.getName,
            file=dirEntry
          ) :: Nil
        case directory if dirEntry.isDirectory =>
          // Recursively crawl the directory
          crawlDirectory(relativeRoot + directory.getName + "/", directory)
        case _ =>
          // Not sure how this happens - device files etc? Java pre-NIO2 isn't very useful here
          Nil
      }
    }
  }

  def apply(rootDir : File) : List[LocalDirectoryEntry] = {
    if (!rootDir.isDirectory) {
      throw new Exception(rootDir.getAbsolutePath() + " is not a directory")
    }

    crawlDirectory("", rootDir)
  }
}
