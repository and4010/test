package com.acelink.etx.totalsecurity.user.tree.strategy

import com.acelink.cloud.tsc.v1.device.data.DeviceListData.DeviceDetailList
import com.acelink.etx.totalsecurity.user.tree.DeviceTree
import com.acelink.etx.totalsecurity.user.tree.UserTree

/**
 * @author gregho
 * @since 2019/1/19
 */
class DeviceStrategy(private val userTree: UserTree) : BuildStrategy<List<DeviceDetailList>> {

  private val deviceTrees = ArrayList<DeviceTree>()

  override fun build(leafs: List<DeviceDetailList>?) {
    leafs?.forEach {
      userTree.findTree(it.path)
          ?.also { tree ->
            val deviceTree = DeviceTree(it.id, it.model).apply {
              bindParent(tree)
            }

            tree.addChild(deviceTree)
            deviceTrees.add(deviceTree)
          }
    }
  }

  fun getDevices() = ArrayList(deviceTrees)

  fun getDeviceCount() = deviceTrees.size

  fun getDevice(index: Int): DeviceTree? {
    if (index < 0 || index >= deviceTrees.size) {
      return null
    }

    return deviceTrees[index]
  }
}