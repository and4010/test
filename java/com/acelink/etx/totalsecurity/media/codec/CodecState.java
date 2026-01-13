package com.acelink.etx.totalsecurity.media.codec;

/**
 * @author gregho
 * @since 2018/10/12
 */
public enum CodecState {

  UNINITIALIZED(),
  PREPARING(),
  PREPARED(),
  FAILED(),
  STOP(),
  RELEASE()
}