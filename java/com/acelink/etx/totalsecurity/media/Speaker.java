package com.acelink.etx.totalsecurity.media;

import android.media.AudioAttributes;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.acelink.etx.totalsecurity.enums.TsRtpFormat;
import com.acelink.etx.totalsecurity.media.codec.AudioDecoder;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;
import com.acelink.etx.totalsecurity.media.codec.MediaFormatBuilder;
import com.acelink.etx.totalsecurity.media.codec.listener.DecodeListener;


public class Speaker implements DecodeListener {

  /* init defines, do not modify */
  private static final String TAG = "Speaker";
  private static final int BYTES_4096 = 4096;
  private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
  private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;

  private final AudioDecoder audioDecoder;
  /* audio speaker */
  private AudioTrack audioTrack;
  /* decides the audio speaker is mute or not */
  private boolean enabled;

  public Speaker(CodecFormat audioFormat) {
    audioDecoder = new AudioDecoder(audioFormat);
    /* default is playing */
    enabled = true;
  }



  public void prepare( int sampleRate,int channelCount) {
    initAudioTrack(sampleRate, channelCount);
    audioDecoder.prepare(sampleRate, channelCount, this);
  }

  /*public void start() {
    try {
      audioTrack.play();
    } catch (Exception ignored) {
    }
    audioDecoder.startCodec();
  }*/

  public void setEos(){
    audioDecoder.setEos();
  }

  public void stop() {

    try {
      if(audioTrack!=null){
      //  Log.e(TAG, "stop AAC audio track");
        audioTrack.pause();
        audioTrack.flush();
        audioTrack.stop();
        audioTrack.release();
        audioTrack=null;
      }

    } catch (Exception ignored) {
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    try {
      if (enabled) {
        audioTrack.play();
      } else {
        audioTrack.pause();
        audioTrack.flush();
      }
    } catch (Exception ignored) {
    }
  }

  public void decode(byte[] content, int contentLength, int sampleRate, int channelCount, long playTimeMs ) {
    audioDecoder.decode(content, contentLength, sampleRate, channelCount/*, playTimeMs */);
  }

  public CodecFormat getCodecFormat() {
    return audioDecoder.getCodecFormat();
  }

  public CodecState getDecoderState() {
    //return audioTrack==null?CodecState.UNINITIALIZED:audioDecoder.getState();
    return audioDecoder.getState();
  }

  private void initAudioTrack(int sampleRate, int channelCount) {
    int channelConfig =
        channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
    /* don't even know what this means, references from Honeywell sdk */
    int customBufferSize = BYTES_4096;
    if (sampleRate == 16000 && channelCount == 2) {
      customBufferSize *= 2;
    }

    int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AUDIO_ENCODING);
    int bufferSize = Math.max(customBufferSize, minBufferSize);
    if(audioTrack!=null){
      stop();
    }
    try {
      audioTrack = getAudioTrack(sampleRate, channelConfig, bufferSize);
      Log.i(TAG, "init audio track");
    } catch (IllegalArgumentException custom) {
      Log.i(TAG,
          "create audio track with buffer size failed, replace with min buffer size");
      try {
        audioTrack = getAudioTrack(sampleRate, channelConfig, minBufferSize);
      } catch (IllegalArgumentException min) {
        Log.i(TAG, "create audio track with min buffer size failed");
      }
    } finally {
      /* if initialized then play */
      if (audioTrack != null && enabled) {
        audioTrack.play();
        Log.i(TAG, "audio track start playing");
      }
    }
  }

  private AudioTrack getAudioTrack(int sampleRate, int channelConfig, int bufferSize) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return new AudioTrack(
          new AudioAttributes.Builder().setLegacyStreamType(STREAM_TYPE).build(),
          new AudioFormat.Builder().setChannelMask(channelConfig)
              .setSampleRate(sampleRate)
              .setEncoding(AUDIO_ENCODING)
              .build(), bufferSize, AUDIO_MODE, AudioManager.AUDIO_SESSION_ID_GENERATE);
    } else {
      return new AudioTrack(STREAM_TYPE, sampleRate, channelConfig, AUDIO_ENCODING, bufferSize, AUDIO_MODE);
    }
  }

  @Override public void onDecode(byte[] chunk, int length) {
    if (enabled) {
      try {
        audioTrack.write(chunk, 0, length);
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
    }
  }

  @Override public void onFormatChanged(MediaFormat format) {
    int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
    if (audioTrack==null||audioTrack.getSampleRate() != sampleRate || audioTrack.getChannelCount() != channelCount) {
      Log.i(TAG, "change audio format");
     /* Log.i(TAG, "current: ["
          + audioTrack.getSampleRate()
          + ", "
          + audioTrack.getChannelCount()
          + "]");*/
      Log.i(TAG, "new: ["
          + sampleRate
          + ", "
          + channelCount
          + "]");
      audioDecoder.onFormatChanged();
      prepare(sampleRate, channelCount);
    }
  }
}