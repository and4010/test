package com.acelink.etx.totalsecurity.user.tree

import com.acelink.etx.totalsecurity.user.v1.TscRole.DEVICE

/**
 * @author gregho
 * @since 2019/1/19
 */
class DeviceTree(
  name: String,
  val model: String
) : LemonTree(name, DEVICE)