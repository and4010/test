package com.acelink.etx.totalsecurity.listener;

import androidx.annotation.NonNull;

/**
 * @author gregho
 * @since 2018/10/22
 */
public interface TsMessageListener {

  void onReceiveMessage(int type, @NonNull String message);
}
