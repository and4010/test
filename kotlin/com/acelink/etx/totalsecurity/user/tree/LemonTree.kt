package com.acelink.etx.totalsecurity.user.tree

import com.acelink.etx.totalsecurity.user.v1.TscRole
import com.acelink.etx.totalsecurity.user.tree.utils.Path

/**
 * @author gregho
 * @since 2019/1/17
 */
abstract class LemonTree(
  val name: String,
  val role: TscRole
) {

  val path = Path.decorate(name)
  private lateinit var parent: LemonTree
  private val childList = ArrayList<LemonTree>()

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (javaClass != other?.javaClass) {
      return false
    }

    other as LemonTree
    return name == other.name
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  /*--------------------------------
   * Parent functions
   *-------------------------------*/

  fun hasParent() = ::parent.isInitialized

  fun getParent() = parent

  fun bindParent(parent: LemonTree) {
    this.parent = parent
  }

  /*--------------------------------
   * Child functions
   *-------------------------------*/

  fun hasChild() = childList.isNotEmpty()

  fun getChildCount() = childList.size

  fun addChild(child: LemonTree) {
    if (!childList.contains(child)) {
      childList.add(child)
    }
  }

  fun getChildAt(index: Int): LemonTree? {
    if (index < 0 || index >= childList.size) {
      return null
    }

    return childList[index]
  }

  fun getAbsolutionPath(): String {
    return if (hasParent()) {
      (if (parent.hasParent()) parent.getAbsolutionPath() else parent.path) + path
    } else {
      Path.SLASH
    }
  }

  fun findTree(path: String): LemonTree? {
    return (digUp(path) ?: digDown(path))
  }

  fun digUp(path: String?): LemonTree? {
    var tree: LemonTree? = null
    if (getAbsolutionPath() == path) {
      tree = this
    } else if (hasParent()) {
      tree = getParent().digUp(path)
    }

    return tree
  }

  fun digDown(path: String?): LemonTree? {
    var tree: LemonTree? = null
    if (getAbsolutionPath() == path) {
      tree = this
    } else if (hasChild()) {
      for (child in childList) {
        tree = child.digDown(path)
        /* break when found one */
        if (tree != null) {
          break
        }
      }
    }

    return tree
  }

  /*--------------------------------
   * Dump functions
   *-------------------------------*/

  fun dump() {
    println(
        """
      |--> START
      |name: $name
      |role: $role
      |hasParent: ${hasParent()}
      |hasChild: ${hasChild()}
      |path: $path
      |absolute path: ${getAbsolutionPath()}
      |<-- END
    """.trimMargin()
    )
  }

  fun dumpParent() {
    if (hasParent()) {
      println("--> START PARENT")
      parent.dump()
      println("<-- END PARENT")
    }
  }

  fun dumpChild() {
    if (hasChild()) {
      println("--> START CHILD")
      for (child in childList) {
        child.dump()
        child.dumpChild()
      }

      println("<-- END CHILD")
    }
  }
}