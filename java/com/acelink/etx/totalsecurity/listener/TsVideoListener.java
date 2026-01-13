package com.acelink.etx.totalsecurity.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.acelink.etx.totalsecurity.enums.TsRtpFormat;

/**
 * @author gregho
 * @since 2018/10/12
 */
interface TsVideoListener {

  void onReceiveVideo(@NonNull String deviceId, @Nullable byte[] content, int contentLength,
      TsRtpFormat.VideoFormat format, int iFrame, int width, int height, long playTimeMs);
}
