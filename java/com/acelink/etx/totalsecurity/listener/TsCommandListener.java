package com.acelink.etx.totalsecurity.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.acelink.etx.totalsecurity.enums.TsJob;
import com.acelink.etx.totalsecurity.enums.TsStatus;

/**
 * @author gregho
 * @since 2018/10/11
 */
interface TsCommandListener {

  void onReceiveCommand(@NonNull String deviceId, @NonNull TsJob job, int customId, @Nullable byte[] content, int contentLength,
      TsStatus status);
}
