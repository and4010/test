package com.acelink.etx.totalsecurity.user.v1

/**
 * @author gregho
 * @since 2019/1/14
 */
enum class TscRole(
  val value: String
) {

  UNKNOWN("unknown") /* unknown role */,
  ROOT("root") /* shouldn't be logged in */,
  ROOT_ADMIN("rootadmin"),
  ADMIN("admin"),
  OPERATOR("operator"),
  DEVICE("device");

  override fun toString(): String {
    return value
  }

  companion object Functions {

    fun fromValue(value: String?): TscRole {
      for (role in values()) {
        if (role.value == value) {
          return role
        }
      }

      return UNKNOWN
    }
  }
}