package com.acelink.etx.totalsecurity.media.rtsp;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.enums.TsRtpFormat;
import com.acelink.etx.totalsecurity.media.Speaker;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;
import com.acelink.etx.totalsecurity.media.codec.VideoDecoder;

import static com.acelink.etx.totalsecurity.enums.TsRtpFormat.AudioFormat.PCM_48K_2;
import static com.acelink.etx.totalsecurity.enums.TsRtpFormat.AudioFormat.PCM_8K_1;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class RtspPlayer {

  private static final String TAG = "RtspHandler";

  private final VideoDecoder videoDecoder;
  private final Speaker speaker;

  private boolean isStreaming=false;

  public RtspPlayer() {
    this(CodecFormat.VIDEO_AVC, CodecFormat.AUDIO_AAC_LC);
  }



  public RtspPlayer(CodecFormat videoFormat, CodecFormat audioFormat) {
    this.videoDecoder = new VideoDecoder(videoFormat);
    this.speaker = new Speaker(audioFormat);
    isStreaming=false;
  }


  public void switchTargetView(TextureView surface) {
      videoDecoder.switchTextureView(surface);
  }

  public void setTextureView(TextureView textureView) {
    videoDecoder.setTextureView(textureView);
  }

  public TextureView getTextureView() {
    return videoDecoder.getTextureView();
  }

  /*--------------------------------
   * Codec functions
   *-------------------------------*/

  public void prepareVideoDecoder(byte[] data, int size) {
      videoDecoder.prepare(data, size);
  }

  public void prepareSpeaker(TsRtpFormat.AudioFormat audioFormat) {
    speaker.prepare(audioFormat.getSampleRate(),audioFormat.getChannelCount());
  }

  public Boolean formatChanged(CodecFormat videoFormat, CodecFormat audioFormat) {
    return videoDecoder.getCodecFormat() != videoFormat || speaker.getCodecFormat() != audioFormat;
  }

  public CodecFormat getVideoFormat() {
    return videoDecoder.getCodecFormat();
  }

  public CodecFormat getSpeakerFormat() {
    return speaker.getCodecFormat();
  }

  /*public void start() {
    videoDecoder.startCodec();
    speaker.start();
  }*/

  public void stop() {
    Log.e("123","stop RTSP PLayer");
    isStreaming=false;
    videoDecoder.setEos();
    speaker.setEos();
    speaker.stop();
    stopPCMPlay();
  }

  public void startStreaming(){
    this.isStreaming=true;
  }

  public boolean isStreaming(){
    return isStreaming;
  }

  public void setSpeakerState(boolean state) {
    speaker.setEnabled(state);
  }

  public boolean getSpeakerState() {
    return speaker.isEnabled();
  }

  public void decodeVideo(byte[] content, int contentLength, int iFrame/*, int width, int height,
      long playTimeMs*/) {
    videoDecoder.decode(content, contentLength, iFrame/*, width, height, playTimeMs*/);
  }

  public void decodeAudio(byte[] content, int contentLength, TsRtpFormat.AudioFormat audioFormat, long playTimeMs) {


    if(audioFormat.getValue()>=PCM_8K_1.getValue()&&audioFormat.getValue()<=PCM_48K_2.getValue()){
      onDecodePCM(content,contentLength,audioFormat);
    }else{//AAC
     // EtxLogger.log(TAG, "TS MESSAGE CALLBACK", "AAC decodeAudio="+ getSpeakerDecoderState());
      if (getSpeakerDecoderState() == CodecState.UNINITIALIZED) {
        prepareSpeaker(audioFormat);
      }
      speaker.decode(content, contentLength, audioFormat.getSampleRate(), audioFormat.getChannelCount(), playTimeMs);
    }

  }

  private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
  private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;
  private static AudioTrack PCMaudioTrack;
  private void initPCMAudioTrack(int sampleRate, int channelCount) {
    int channelConfig =
            channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
    /* don't even know what this means, references from Honeywell sdk */
    int customBufferSize = 4096;
    if (sampleRate == 16000 && channelCount == 2) {
      customBufferSize *= 2;
    }

    int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AUDIO_ENCODING);
    int bufferSize = Math.max(customBufferSize, minBufferSize);
    try {
      Log.i(TAG, "init PCM audio track");
      PCMaudioTrack = getAudioTrack(sampleRate, channelConfig, bufferSize);

    } catch (IllegalArgumentException custom) {
      Log.i(TAG, "create PCM audio track with buffer size failed, replace with min buffer size");
      try {
        PCMaudioTrack = getAudioTrack(sampleRate, channelConfig, minBufferSize);
      } catch (IllegalArgumentException min) {
        Log.i(TAG, "create PCM audio track with min buffer size failed");
      }
    } finally {
      /* if initialized then play */
      if (PCMaudioTrack != null && isStreaming) {
        PCMaudioTrack.play();
        Log.i(TAG, "PCM audio track start playing");
      }else {
        Log.e(TAG, "init PCMaudioTrackisStreaming="+isStreaming+",PCMaudioTrack is null?"+(PCMaudioTrack==null));
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
      return new AudioTrack(STREAM_TYPE, sampleRate, channelConfig, AUDIO_ENCODING, bufferSize,
              AUDIO_MODE);
    }
  }

  public void stopPCMPlay() {

    try {
      if(PCMaudioTrack!=null){
        Log.e(TAG, "stopPCMPlay");
        PCMaudioTrack.pause();
        PCMaudioTrack.flush();
        PCMaudioTrack.stop();
        PCMaudioTrack.release();
        PCMaudioTrack=null;
      }

    } catch (Exception ignored) {
    }
  }

  private synchronized void onDecodePCM(byte[] chunk, int length, TsRtpFormat.AudioFormat audioFormat) {

    if (isStreaming)
    {
      if(PCMaudioTrack==null||PCMaudioTrack.getSampleRate()!=audioFormat.getSampleRate()||PCMaudioTrack.getChannelCount()!=audioFormat.getChannelCount())
      {
        EtxLogger.log(getClass().getSimpleName(),"PCM Audio","initPCMAudioTrack "+(PCMaudioTrack==null?"null":("old sample="+PCMaudioTrack.getSampleRate()+",oldChannel="+PCMaudioTrack.getChannelCount())));
        initPCMAudioTrack(audioFormat.getSampleRate(),audioFormat.getChannelCount());
      }
        try {
          PCMaudioTrack.write(chunk, 0, length);
        } catch (Exception ignored) {
          ignored.printStackTrace();
        }

    }
  }

  /*--------------------------------
   * State functions
   *-------------------------------*/

  public CodecState getVideoDecoderState() {
    return videoDecoder.getState();
  }

  public CodecState getSpeakerDecoderState() {
    return speaker.getDecoderState();
  }

  public int getRawDataWidth() {
    return videoDecoder.getRawDataWidth();
  }

  public int getRawDataHeight() {
    return videoDecoder.getRawDataHeight();
  }
}