package com.acelink.etx.totalsecurity.user.tree.strategy

import com.acelink.cloud.tsc.v1.user.data.UserData
import com.acelink.etx.totalsecurity.user.v1.TscRole
import com.acelink.etx.totalsecurity.user.v1.TscRole.UNKNOWN
import com.acelink.etx.totalsecurity.user.tree.UserTree
import com.acelink.etx.totalsecurity.user.tree.utils.Path

/**
 * @author gregho
 * @since 2019/1/19
 */
class UserStrategy(private val userTree: UserTree) : BuildStrategy<List<UserData>> {

  fun definePath(path: String?) {
    /* you might have no parent */
    if (path == null) {
      return
    }

    val split = path.split(Path.SLASH)
    val size = split.size
    var child = userTree
    for (i in size - 1 downTo 0) {
      val name = split[i]
      if (name.isBlank()) {
        /* skip blank */
        continue
      }

      val parent = UserTree(name, UNKNOWN /* you don't know your parent's role */).apply {
        addChild(child)
      }

      child.bindParent(parent)
      child = parent
    }
  }

  override fun build(
    leafs: List<UserData>?
  ) {
    /* you might have no child */
    leafs?.run {
      grow(userTree, ArrayList(this))
    }
  }

  private fun grow(
    tree: UserTree,
    leafs: ArrayList<UserData>
  ) {
    val iterator = leafs.iterator()
    while (iterator.hasNext()) {
      iterator.next()
          .also { userData ->
            val userId = userData.userId ?: run {
              iterator.remove()
              return@also
            }

            val role = userData.role ?: run {
              iterator.remove()
              return@also
            }

            val path = userData.path ?: run {
              iterator.remove()
              return@also
            }

            if (path == tree.getAbsolutionPath()) {
              /* found matched, remove this leaf */
              iterator.remove()
              /* add child and link self as parent */
              userTree.addChild(UserTree(userId, TscRole.fromValue(role)).apply {
                bindParent(tree)
                /* grow child, pass the new leafs to avoid CME */
                grow(this, ArrayList(leafs))
              }
              )
            }
          }
    }
  }
}