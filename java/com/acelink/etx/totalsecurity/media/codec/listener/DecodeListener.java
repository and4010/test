package com.acelink.etx.totalsecurity.media.codec.listener;

import android.media.MediaFormat;

/**
 * @author gregho
 * @since 2018/12/7
 */
public interface DecodeListener {

  void onDecode(byte[] chunk, int length);

  void onFormatChanged(MediaFormat format);
}
