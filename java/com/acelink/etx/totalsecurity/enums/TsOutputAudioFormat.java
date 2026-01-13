package com.acelink.etx.totalsecurity.enums;

/**
 * @author gregho
 * @since 2019/1/28
 */
public enum TsOutputAudioFormat {
  AAC_8K_1(0, 8_000, 1),//no use
  AAC_16K_1(0, 16_000, 1),
  AAC_48K_1(1, 48_000, 1),
  PCM_16K_1(2, 16_000, 1);


  private final int value;
  private final int sampleRate;
  private final int channelCount;

  TsOutputAudioFormat(int value, int sampleRate, int channelCount) {
    this.value = value;
    this.sampleRate = sampleRate;
    this.channelCount = channelCount;
  }

  public int getValue() {
    return value;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public int getChannelCount() {
    return channelCount;
  }
}
