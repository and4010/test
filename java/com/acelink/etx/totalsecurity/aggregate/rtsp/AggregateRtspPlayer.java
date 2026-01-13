package com.acelink.etx.totalsecurity.aggregate.rtsp;

import static com.acelink.etx.totalsecurity.enums.TsRtpFormat.AudioFormat.PCM_48K_2;
import static com.acelink.etx.totalsecurity.enums.TsRtpFormat.AudioFormat.PCM_8K_1;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.FrameFPS;
import com.acelink.etx.totalsecurity.aggregate.AggregateSpeaker;

import com.acelink.etx.totalsecurity.aggregate.codec.AggregateVideoDecoder;
import com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode;
import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type;
import com.acelink.etx.totalsecurity.enums.TsRtpFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class AggregateRtspPlayer {

  private static final String TAG = "RtspHandler";

  private boolean typeLAN=false;

  private final AggregateVideoDecoder videoDecoder;
  private final AggregateSpeaker speaker;

  private boolean isStreaming=false;

  public AggregateRtspPlayer() {
    this(CodecFormat.VIDEO_AVC, CodecFormat.AUDIO_AAC_LC);
  }



  public AggregateRtspPlayer(CodecFormat videoFormat, CodecFormat audioFormat) {
    this.videoDecoder = new AggregateVideoDecoder(videoFormat);
    this.speaker = new AggregateSpeaker(audioFormat);
    isStreaming=false;
  }


  public void switchTargetView(TextureView surface) {
      videoDecoder.switchTextureView(surface);
  }

  public void setFPS(FrameFPS f){
    videoDecoder.setFPS(f);
  }

  public void setTextureView(TextureView textureView) {
    videoDecoder.setTextureView(textureView);
  }

  public void setSnapShotImage(ImageView snapshot)
  {
    videoDecoder.setSnapShotImage(snapshot);
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
    EtxLogger.log(TAG,"123","stop AggregateRtspPlayer RTSP PLayer typeLAN="+typeLAN);
    isStreaming=false;
    videoDecoder.setEos();
    speaker.setEos();
    speaker.stop();
    stopPCMPlay();
  }

  public long getReceiveVideoRealFrameDiff(){
    return videoDecoder.getReceiveRealFrameDiff();
  }

  public void startStreaming(){
    this.isStreaming=true;

  }

  public void unVisableSnapshotForLan(){
    videoDecoder.unVisableSnapshot();//Lan streaming may no format change
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

  public void decodeVideo(byte[] content, int contentLength, int iFrame/*, int width, int height*/
      ,long playTimeMs)
  {
    videoDecoder.decode(content, contentLength, iFrame, playTimeMs/*, width, height*/);
  }

  public void decodeAudio(byte[] content, int contentLength, TsRtpFormat.AudioFormat audioFormat, long playTimeMs) {


    if(audioFormat.getValue()>=PCM_8K_1.getValue()&&audioFormat.getValue()<=PCM_48K_2.getValue()){
      onDecodePCM(content,contentLength,audioFormat);
    }else{//AAC
     // EtxLogger.log(TAG, "TS MESSAGE CALLBACK", "AAC audioFormat="+audioFormat+",isEnabled="+speaker.isEnabled()+", decodeAudio="+ getSpeakerDecoderState()+"\n,contentLength="+contentLength);
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

  public Dewrap_mode getDewrapMode(){
   return videoDecoder.getDewrapMode();
  }

  public void setMountType(Mount_type type){
    videoDecoder.setMountType(type);
  }

  public void setDewrapMode1O(){
    videoDecoder.setDewrapMode1O();
  }

  public void setDewrapMode1OToWall1R(){
    videoDecoder.setDewrapMode1OToWall1R();
  }

  public void setDewrapMode1P(){
    videoDecoder.setDewrapMode1P();
  }



  public void setDewrapMode1OTo1P(ImageView img1){
    videoDecoder.setDewrapMode1OTo1P(img1);
  }

  public void setDewrapMode1OToWall1P(ImageView img1){
    videoDecoder.setDewrapMode1OToWall1P(img1);
  }


  public void setDewrapMode1OTo1R(ImageView img1){
    videoDecoder.setDewrapMode1OTo1R(img1);
  }

  public void setDewrapMode1OTo1R2(ImageView img1){
    videoDecoder.setDewrapMode1OTo1R2(img1);
  }

  public void setDewrapMode1OTo4R(ImageView img1, ImageView img2, ImageView img3, ImageView img4){
    videoDecoder.setDewrapMode1OTo4R(img1,img2,img3,img4);
  }


  public boolean getTypeLAN() {
    return typeLAN;
  }

  public void setTypeLAN(boolean typeLAN) {
    this.typeLAN = typeLAN;
  }
}