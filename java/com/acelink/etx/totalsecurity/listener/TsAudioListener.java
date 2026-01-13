package com.acelink.etx.totalsecurity.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.acelink.etx.totalsecurity.enums.TsRtpFormat;

/**
 * @author gregho
 * @since 2018/10/12
 */
interface TsAudioListener {

  void onReceiveAudio(@NonNull String deviceId, @Nullable byte[] content, int contentLength,
      TsRtpFormat.AudioFormat format, long playTimeMs);
}
