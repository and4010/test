package com.acelink.etx.totalsecurity.media.codec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import androidx.annotation.RequiresApi;
import java.nio.ByteBuffer;

/**
 * @author gregho
 * @since 2018/12/6
 */
public class MediaFormatBuilder {

  private final MediaFormat mediaFormat;

  private MediaFormatBuilder(MediaFormat mediaFormat) {
    this.mediaFormat = mediaFormat;
  }

  public static MediaFormatBuilder videoFormat(String mimeType, int width, int height) {
    return new MediaFormatBuilder(MediaFormat.createVideoFormat(mimeType, width, height));
  }

  public static MediaFormatBuilder audioFormat(String mimeType, int sampleRate, int channelCount) {
    return new MediaFormatBuilder(
        MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount));
  }

  public MediaFormatBuilder setBitRate(int bitRate) {
    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
    return this;
  }

  public MediaFormatBuilder setByteBuffer(String name, ByteBuffer buffer) {
    mediaFormat.setByteBuffer(name, buffer);
    return this;
  }

  public MediaFormatBuilder setMaxInputSize(int size) {
    mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, size);
    return this;
  }

  public MediaFormatBuilder setAacProfile(int aacProfile) {
    mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
    return this;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public MediaFormatBuilder setAacSbrMode(int mode) {
    mediaFormat.setInteger(MediaFormat.KEY_AAC_SBR_MODE, mode);
    return this;
  }

  public MediaFormatBuilder enableLowLatency() //no use
 {
   /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)//fujitsu -F-02L waill fail
   {
       mediaFormat.removeFeature(MediaCodecInfo.CodecCapabilities.FEATURE_LowLatency);
       mediaFormat.setInteger(MediaFormat.KEY_LOW_LATENCY, 1);
   }*/
     //mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedHigh);
     mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
     mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31);

     return this;
  }

  public MediaFormatBuilder colorSurface()
  {
      mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
    //  mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
    return this;
  }

  public MediaFormat build() {
    return mediaFormat;
  }
}