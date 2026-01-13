package com.acelink.etx.totalsecurity.enums;

import androidx.annotation.NonNull;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class TsRtpFormat {

  public enum VideoFormat {
/*
                              case 5: // Coded slice of an IDR picture
                                    *nIFrame = 1;
                                    break;
                                case 7: // Sequence parameter set (SPS)
                                case 8: // Picture parameter set (PPS)
                                    *nIFrame = 2;
                                    break;
                                case 6: // Supplemental enhancement information (SEI)
                                    *nIFrame = 3;
                                    break;
                                case 1: // Coded slice of a non-IDR picture
                                default:
                                    *nIFrame = 0;
                                    break;
* */
    UNKNOWN(-1),
    BITMAP(0),
    JPEG(1),
    H264(2);

    private final int value;

    VideoFormat(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static VideoFormat fromValue(int value) {
      for (VideoFormat format : values()) {
        if (format.value == value) {
          return format;
        }
      }

      return UNKNOWN;
    }
  }
/*
LAN Pstream
enum PT_AUDIO_FORAMT
{
    PT_AUDIO_FORMAT_AAC_8000_1 = 0,
    PT_AUDIO_FORMAT_AAC_8000_2,
    PT_AUDIO_FORMAT_AAC_16000_1,
    PT_AUDIO_FORMAT_AAC_16000_2,
    PT_AUDIO_FORMAT_AAC_32000_1,
    PT_AUDIO_FORMAT_AAC_32000_2,
    PT_AUDIO_FORMAT_AAC_48000_1,
    PT_AUDIO_FORMAT_AAC_48000_2,
    PT_AUDIO_FORMAT_PCM_8000_1,
    PT_AUDIO_FORMAT_PCM_8000_2,
    PT_AUDIO_FORMAT_PCM_16000_1,
    PT_AUDIO_FORMAT_PCM_16000_2,
    PT_AUDIO_FORMAT_PCM_32000_1,
    PT_AUDIO_FORMAT_PCM_32000_2,
    PT_AUDIO_FORMAT_PCM_48000_1,
    PT_AUDIO_FORMAT_PCM_48000_2,
    PT_AUDIO_FORMAT_A_LAW,
    PT_AUDIO_FORMAT_U_LAW,
    PT_AUDIO_FORMAT_ADPCM
}
 */
  public enum AudioFormat {

    UNKNOWN(-1, 0, 0),
    AAC_8K_1(0, 8_000, 1),
    AAC_8K_2(1, 8_000, 2),
    AAC_16K_1(2, 16_000, 1),
    AAC_16K_2(3, 16_000, 2),
    AAC_32K_1(4, 32_000, 1),
    AAC_32K_2(5, 32_000, 2),
    AAC_48K_1(6, 48_000, 1),
    AAC_48K_2(7, 48_000, 2),
    PCM_8K_1(8, 8_000, 1),
    PCM_8K_2(9, 8_000, 2),
    PCM_16K_1(10, 16_000, 1),
    PCM_16K_2(11, 16_000, 2),
    PCM_32K_1(12, 32_000, 2),
    PCM_32K_2(13, 32_000, 1),
    PCM_48K_1(14, 48_000, 1),
    PCM_48K_2(15, 48_000, 2),
    A_LAW(16, 8_000, 1),
    U_LAW(17, 8_000, 1),
    AD_PCM(18, 0, 0);

    private final int value;
    private final int sampleRate;
    private final int channelCount;

    AudioFormat(int value, int sampleRate, int channelCount) {
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

    @NonNull public static AudioFormat fromValue(int value) {
      for (AudioFormat format : values()) {
        if (format.value == value) {
          return format;
        }
      }

      return UNKNOWN;
    }
  }
}
