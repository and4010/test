package com.acelink.etx.totalsecurity.media.codec;

import android.media.MediaCodec;
import android.os.Build;
import androidx.annotation.WorkerThread;
import android.util.Log;
import com.acelink.etx.totalsecurity.media.codec.listener.EncodeListener;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.acelink.etx.totalsecurity.media.codec.CodecConstants.ADTS_SIZE;

/**
 * @author gregho
 * @since 2018/12/5
 */
public class AudioEncoder extends BaseCodec {

  /* init defines, do not modify */
  private static final String TAG = "AudioEncoder";

  /* audio microphone */
  private int sampleRate;
  private int channelCount;
  private int bitRate;
  private int frequencyIndex;
  private EncodeListener listener;

  public AudioEncoder(CodecFormat codecFormat) {
    super(codecFormat);
  }

  /*--------------------------------
   * Codec functions
   *-------------------------------*/

  public void prepare(int sampleRate, int channelCount, int bitRate, EncodeListener listener) {
    synchronized (this) {
      setState(CodecState.PREPARING);
      this.listener = listener;
      this.sampleRate = sampleRate;
      this.channelCount = channelCount;
      this.bitRate = bitRate;
      this.frequencyIndex = CodecConstants.getFrequencyIndex(sampleRate);
      initMediaFormat();
      if (isState(CodecState.FAILED)) {
        /* return when crate audio record failed */
        return;
      }

      initCodec();
    }
  }

  @Override protected void initMediaFormat() {
    MediaFormatBuilder builder =
        MediaFormatBuilder.audioFormat(getMimeType(), sampleRate, channelCount)
            .setAacProfile(getProfile()).setBitRate(bitRate);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.setAacSbrMode(0);
    }

    setFormat(builder.build());
  }

  @Override protected void initCodec() {
    try {
      MediaCodec codec = MediaCodec.createEncoderByType(getMimeType());
      codec.configure(getFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
      setCodec(codec);
      startCodec();
    } catch (IOException e) {
      setState(CodecState.FAILED);
      e.printStackTrace();
      Log.i(TAG, "INIT CODEC -> failed, I/O exception");
    }
  }

  /**
   * Encodes the raw audio data
   *
   * @param content audio data
   * @param contentLength length of audio data
   * @param playTimeMs play time
   */
  @WorkerThread
  public void encode(byte[] content, int contentLength, long playTimeMs) {
    synchronized (this) {
//      setContent(content);
//      setContentLength(contentLength);
      try {

        if (content != null && contentLength > 0) {
          int inputBufferIndex = getCodec().dequeueInputBuffer(TIMEOUT);
          if (inputBufferIndex >= 0) {
            if (isEos()) {
              getCodec().queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
              ByteBuffer inputBuffer;
              inputBuffer = getCodec().getInputBuffer(inputBufferIndex);

              if (inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(content, 0, contentLength);
                getCodec().queueInputBuffer(inputBufferIndex, 0, contentLength, 0L, 0);
              }
            }
          }

          setContent(null);
          setContentLength(0);
        }
      } catch (Exception ignored) {
      }

      //decode
     /* try {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = getCodec().dequeueOutputBuffer(bufferInfo, TIMEOUT);
        switch (outputBufferIndex) {
          case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
            break;

          case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
            break;

          case MediaCodec.INFO_TRY_AGAIN_LATER:
            break;

          default:
            ByteBuffer outputBuffer = getCodec().getOutputBuffers()[outputBufferIndex];
            int outputBufferSize = bufferInfo.size;
            if (outputBufferSize > 10 ) {
              int chunkSize = outputBufferSize + ADTS_SIZE;
              byte[] chunk = new byte[chunkSize];
              byte[] adst =
                      CodecConstants.getAdts(getProfile(), frequencyIndex, channelCount, chunkSize);
              System.arraycopy(adst, 0, chunk, 0, ADTS_SIZE);
              int offset = bufferInfo.offset;
              outputBuffer.position(offset);
              outputBuffer.limit(outputBufferSize + offset);
              outputBuffer.get(chunk, ADTS_SIZE, outputBufferSize);
              if (listener != null) {
                listener.onEncode(chunk, chunkSize);
              }

              outputBuffer.clear();
            }

            getCodec().releaseOutputBuffer(outputBufferIndex, false);
            break;
        }

        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
          Log.i(TAG, "MEET FLAG -> `END OF STREAM`");
          if (listener != null) {
            listener.onStop();
          }

          stopCodec();
          releaseCodec();
        }
      } catch (Exception ignored) {
      }*/
    }
  }

  @Override void process() {
    /*
    try {
      byte[] content = getContent();
      int contentLength = getContentLength();
      if (content != null && contentLength > 0) {
        int inputBufferIndex = getCodec().dequeueInputBuffer(TIMEOUT);
        if (inputBufferIndex >= 0) {
          if (isEos()) {
            getCodec().queueInputBuffer(inputBufferIndex, 0, 0, 0,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
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

        setContent(null);
        setContentLength(0);
      }
    } catch (Exception ignored) {
    }
    */
  }

  @Override void output() {
   /* try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }*/

    try {
      MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
      int outputBufferIndex = getCodec().dequeueOutputBuffer(bufferInfo, TIMEOUT);
      switch (outputBufferIndex) {
        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:

          break;

        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:

          break;

        case MediaCodec.INFO_TRY_AGAIN_LATER:

          break;

        default:
          ByteBuffer outputBuffer = getCodec().getOutputBuffer(outputBufferIndex);
          int outputBufferSize = bufferInfo.size;
          if (outputBufferSize > 10 ) {
            int chunkSize = outputBufferSize + ADTS_SIZE;
            byte[] chunk = new byte[chunkSize];
            byte[] adst = CodecConstants.getAdts(getProfile(), frequencyIndex, channelCount, chunkSize);

            System.arraycopy(adst, 0, chunk, 0, ADTS_SIZE);
            int offset = bufferInfo.offset;
            outputBuffer.position(offset);
            outputBuffer.limit(outputBufferSize + offset);

            outputBuffer.get(chunk, ADTS_SIZE, outputBufferSize);
            if (listener != null) {
              listener.onEncode(chunk, chunkSize);
            }

            outputBuffer.clear();
          }

          getCodec().releaseOutputBuffer(outputBufferIndex, false);
          break;
      }

      if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
        Log.i(TAG, "MEET FLAG -> `END OF STREAM`");
        if (listener != null) {
          listener.onStop();
        }

        stopCodec();
        releaseCodec();
      }
    } catch (Exception ignored) {
    }
  }

  @Override public void startCodec() throws NullPointerException {
    if (isState(CodecState.PREPARING) || isState(CodecState.STOP)) {
      try {
        setState(CodecState.PREPARED);
        super.startCodec();
        Log.i(TAG, "START AUDIO ENCODER CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.i(TAG, "START AUDIO ENCODER  CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        setState(CodecState.UNINITIALIZED);
        Log.i(TAG, "START AUDIO ENCODER  CODEC -> failed, not configured");
      }
    } else {
      Log.i(TAG, "START AUDIO ENCODER  CODEC -> failed, illegal state");
    }
  }

  @Override void stopCodec() {
    /* FIXME: should considering preparing */
    if (isState(CodecState.PREPARED)) {
      try {
        setState(CodecState.STOP);
        super.stopCodec();
        Log.i(TAG, "STOP CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.i(TAG, "STOP CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        e.printStackTrace();
        Log.i(TAG, "STOP CODEC -> failed, is in release state");
      }
    } else {
      Log.i(TAG, "STOP CODEC -> failed, illegal state");
    }
  }

  @Override void releaseCodec() {
    if (isState(CodecState.STOP)) {
      try {
        setState(CodecState.RELEASE);
        super.releaseCodec();
        Log.i(TAG, "RELEASE CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.i(TAG, "RELEASE CODEC -> failed, no instance");
      }
    } else {
      Log.i(TAG, "RELEASE CODEC -> failed, illegal state");
    }
  }
}