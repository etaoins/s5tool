package com.github.etaoins.s5tool

import java.security.MessageDigest

object LocalFileHasher {
  def apply(encoded : EncodedLocalFile) : HashedLocalFile = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.update(encoded.body)

    HashedLocalFile(
      siteRelativePath=encoded.siteRelativePath,
      contentEncoding=encoded.contentEncoding,
      body=encoded.body,
      contentMd5=md5.digest)
  }
}

