package com.acelink.etx.totalsecurity.user.tree

import com.acelink.etx.totalsecurity.user.v1.TscRole
import com.acelink.etx.totalsecurity.user.v1.TscRole.DEVICE
import com.acelink.etx.totalsecurity.user.tree.strategy.DeviceStrategy
import com.acelink.etx.totalsecurity.user.tree.strategy.UserStrategy

/**
 * @author gregho
 * @since 2019/1/19
 */
class UserTree(
  name: String,
  role: TscRole
) : LemonTree(name, role) {

  val userStrategy = UserStrategy(this)
  val deviceStrategy = DeviceStrategy(this)

  fun hasDevice(): Boolean {
    for (i in 0 until getChildCount()) {
      if (getChildAt(i)?.role == DEVICE) {
        return true
      }
    }

    return false
  }
}

/*
fun main(args: Array<String>) {
  val path = "/a"
  val user = "b"
  val descendants = listOf(
      UserData.Builder().userId("c").role("admin").path("$path/$user").build(),
      UserData.Builder().userId("d").role("operator").path("$path/$user/c").build(),
      UserData.Builder().userId("e").role("admin").path("$path/$user").build()
  )

  val tree = UserTree(user, ROOT_ADMIN).apply {
    with(userStrategy) {
      definePath(path)
      build(descendants)
    }
  }
}*/
