package com.acelink.etx.totalsecurity.media.codec.listener;

/**
 * @author gregho
 * @since 2018/12/5
 */
public interface EncodeListener {

  void onEncode(byte[] chunk, int length);

  void onStop();
}
