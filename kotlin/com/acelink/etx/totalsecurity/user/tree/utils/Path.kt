package com.acelink.etx.totalsecurity.user.tree.utils

/**
 * @author gregho
 * @since 2019/1/21
 */
object Path {

  const val SLASH = "/"

  fun decorate(value: String): String {
    return if (legalPath(value)) {
      value
    } else {
      "$SLASH$value"
    }
  }

  fun legalPath(path: String): Boolean {
    return path.startsWith(SLASH)
  }
}