package com.acelink.etx.totalsecurity.aggregate.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.media.codec.CodecConstants;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;
import com.acelink.etx.totalsecurity.media.codec.MediaFormatBuilder;
import com.acelink.etx.totalsecurity.media.codec.listener.DecodeListener;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author gregho
 * @since 2018/10/22
 *
 * <p>
 * Support:
 * 1. AAC LC
 * </p>
 */
public class AggregateAudioDecoder extends AggregateBaseCodec {

  /* init defines, do not modify */
  private static final String TAG = "AggregateAudioDecoder";

  private int sampleRate;
  private int channelCount;
  private DecodeListener listener;

  public AggregateAudioDecoder(CodecFormat codecFormat) {
    super(codecFormat);
  }

  /*--------------------------------
   * Codec functions
   *-------------------------------*/

  public void prepare(int sampleRate, int channelCount, DecodeListener listener) {
    synchronized (this) {
      setState(CodecState.PREPARING);
      this.listener = listener;
      this.sampleRate = sampleRate;
      this.channelCount = channelCount;
      initMediaFormat();
      if (listener != null) {
        listener.onFormatChanged(
                MediaFormatBuilder.audioFormat(getMimeType(), sampleRate, channelCount).build());
      }
      if (isState(CodecState.FAILED)) {
        /* return when create media format failed */
        return;
      }

      initCodec();
    }
  }

  public void setPCMPrepared(){
    setState(CodecState.PREPARED);
  }

  @Override protected void initMediaFormat() {
    int aacProfile = getProfile();
    MediaFormat format = MediaFormatBuilder.audioFormat(getMimeType(), sampleRate, channelCount)
        .setAacProfile(aacProfile)
        .setByteBuffer(CSD_0, CodecConstants.getAacCsd0(sampleRate, channelCount, aacProfile))
        .build();
    if (format == null) {
      Log.i(TAG, "INIT MEDIA FORMAT -> failed, no such audio format");
      setState(CodecState.FAILED);
    }

    Log.i(TAG, "INIT MEDIA FORMAT -> succeeded");
    setFormat(format);
  }

  @Override protected void initCodec() {
    try {
      MediaCodec codec = MediaCodec.createDecoderByType(getMimeType());
      codec.configure(getFormat(), null, null, 0);
      setCodec(codec);
      startCodec();
    } catch (IOException e) {
      setState(CodecState.FAILED);
      e.printStackTrace();
      Log.i(TAG, "INIT CODEC -> failed, I/O exception");
    }
  }

  /**
   * Decodes the raw audio data
   * TODO: should implement skip audio data when decode too slow mechanism?
   *
   * @param content audio data
   * @param contentLength length of audio data
   * @param sampleRate sample rate of audio
   * @param channelCount channel count of audio
   * //@param playTimeMs play time
   */
  @WorkerThread
  public void decode(byte[] content, int contentLength, int sampleRate,
      int channelCount/*, long playTimeMs */) {
    synchronized (this) {
      setContent(content);
      setContentLength(contentLength);
      this.sampleRate = sampleRate;
      this.channelCount = channelCount;
    }
  }

  @Override void process() {
    /* update audio format */
    try {
      byte[] content = getContent();
      int contentLength = getContentLength();
      if (content != null && contentLength > 0) {
        int inputBufferIndex = getCodec().dequeueInputBuffer(TIMEOUT);
      //  EtxLogger.logE(TAG, "AggregateAudioDecoder", "AggregateAudioDecoder inputBufferIndex="+inputBufferIndex);
        if (inputBufferIndex >= 0) {
          if (isEos()) {
            getCodec().queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
          } else {
            ByteBuffer inputBuffer;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
              inputBuffer = getCodec().getInputBuffers()[inputBufferIndex];
            } else {
              inputBuffer = getCodec().getInputBuffer(inputBufferIndex);
            }

            if (inputBuffer != null) {
              inputBuffer.clear();
              inputBuffer.put(content, 0, contentLength);
              getCodec().queueInputBuffer(inputBufferIndex, 0, contentLength, 0L, 0);
            }
          }
        }
        /* clear decode content */
        setContent(null);
        setContentLength(0);
      }
    } catch (Exception ignored) {
    }
  }

  @Override void output() {
    try {
      MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
      int outputBufferIndex = getCodec().dequeueOutputBuffer(bufferInfo, TIMEOUT);
      switch (outputBufferIndex) {
        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
          /* ignored */
          break;

        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
          /* update audio format */
          if (listener != null) {
            listener.onFormatChanged(getCodec().getOutputFormat());
          }

          break;

        case MediaCodec.INFO_TRY_AGAIN_LATER:
          /* ignored */
          break;

        default:

          ByteBuffer outputBuffer = getCodec().getOutputBuffer(outputBufferIndex);
          int outputBufferSize = bufferInfo.size;
          byte[] chunk = new byte[outputBufferSize];
          outputBuffer.get(chunk);
          outputBuffer.clear();
          if (listener != null && outputBufferSize > 0) {
            //EtxLogger.logE(TAG, "AggregateAudioDecoder", "AggregateAudioDecoder default outputBufferIndex="+outputBufferIndex+"outputBufferSize="+outputBufferSize);
            listener.onDecode(chunk, outputBufferSize);
          }

          getCodec().releaseOutputBuffer(outputBufferIndex, false);
          break;
      }

      if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
        Log.e(TAG, "MEET FLAG AggregateAudioDecoder-> `END OF STREAM`");
        stopCodec();
        releaseCodec();
      }
    } catch (Exception ignored) {
      //ignored.printStackTrace();
      try {
        Thread.sleep(2);
      }catch (Exception ignored2){}
    }
  }

  @Override public void startCodec() throws NullPointerException {
    if (isState(CodecState.PREPARING) || isState(CodecState.STOP)) {
      try {
        setState(CodecState.PREPARED);
        super.startCodec();
        Log.i(TAG, "AggregateAudioDecoder START AUDIO DECODER CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "AggregateAudioDecoder START AUDIO DECODER CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "AggregateAudioDecoder START AUDIO DECODER CODEC -> failed, not configured");
      }
    } else {
      Log.e(TAG, "START AggregateAudioDecoder -> failed, illegal state");
    }
  }

  @Override void stopCodec() {
    /* FIXME: should considering preparing */
    if (isState(CodecState.PREPARED)) {
      try {
        setState(CodecState.STOP);
        super.stopCodec();
        Log.i(TAG, "STOP AggregateAudioDecoder -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "STOP AggregateAudioDecoder -> failed, no instance");
      } catch (IllegalStateException e) {
        e.printStackTrace();
        Log.e(TAG, "STOP AggregateAudioDecoder -> failed, is in release state");
      }
    } else {
      Log.e(TAG, "STOP AggregateAudioDecoder -> failed, illegal state");
    }
  }

  @Override void releaseCodec() {
    if (isState(CodecState.STOP)) {
      try {
        setState(CodecState.RELEASE);
        super.releaseCodec();
        Log.i(TAG, "RELEASE AggregateAudioDecoder -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "RELEASE AggregateAudioDecoder -> failed, no instance");
      }
    } else {
      Log.e(TAG, "RELEASE AggregateAudioDecoder -> failed, illegal state");
    }
  }
}