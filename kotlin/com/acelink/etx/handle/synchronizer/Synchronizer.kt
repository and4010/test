package com.acelink.etx.handle.synchronizer

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author gregho
 * @since 2018/12/3
 */
class Synchronizer {

  companion object Functions {

    /**
     * Synchronizes two list, invoked [block] decides the src and dst item is the equals or not
     */
    inline fun <T, U> isDifferent(
      srcList: List<T>,
      dstList: List<U>,
      block: (src: T, dst: U) -> Boolean
    ): Boolean {
      val srcListSize = srcList.size
      val dstListSize = dstList.size
      if (srcListSize != dstListSize) {
        return true
      }

      var index = 0
      while (index < dstListSize) {
        if (!block(srcList[index], dstList[index])) {
          return true
        }

        index++
      }

      return false
    }

    /**
     * Removes nonexistent item in src list, invoked [block] decides the src item is deprecated or not
     */
    inline fun <T, U> removeNonexistent(
      srcList: CopyOnWriteArrayList<T>,
      dstList: List<U>,
      block: (src: T, dst: U) -> Boolean,
      removed: (src: T) -> Unit
    ) {
      srcList.iterator()
          .also { iterator ->
            while (iterator.hasNext()) {
              val src = iterator.next()
              var deprecated = true
              dstList.forEach check@{ dst ->
                if (block(src, dst)) {
                  deprecated = false
                  return@check
                }
              }

              if (deprecated) {
                srcList.remove(src)
                removed(src)
              }
            }
          }
    }
  }
}