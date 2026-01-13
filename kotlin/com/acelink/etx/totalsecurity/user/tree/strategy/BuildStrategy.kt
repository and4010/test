package com.acelink.etx.totalsecurity.user.tree.strategy

/**
 * @author gregho
 * @since 2019/1/21
 */
interface BuildStrategy<T: Any> {

  fun build(
    leafs: T?
  )
}