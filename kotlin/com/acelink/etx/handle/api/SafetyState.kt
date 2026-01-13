package com.acelink.etx.handle.api

/**
 * @author gregho
 * @since 2018/9/25
 */
enum class SafetyState {

  UNINITIALIZED(),
  INITIALIZING(),
  FAILED(),
  INITIALIZED();
}