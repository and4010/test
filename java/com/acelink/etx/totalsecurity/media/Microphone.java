package com.acelink.etx.totalsecurity.media;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.media.codec.AudioEncoder;
import com.acelink.etx.totalsecurity.media.codec.AudioHEncode;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;
import com.acelink.etx.totalsecurity.media.codec.listener.EncodeHListener;
import com.acelink.etx.totalsecurity.media.codec.listener.EncodeListener;
import com.acelink.etx.totalsecurity.media.utils.MediaWriter;
import java.lang.ref.WeakReference;


public class Microphone implements EncodeListener {

  public interface RecordListener {
    void onStartMic();
    void onSpeaking(byte[] chunk, int length);

    void onTurnOff();
  }

  /* init defines, do not modify */
  private static final String TAG = "Microphone";
  private static final int BIT_RATE = 16000;
  private static final int BYTES_2048 = 2048;
  private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
  private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  private final AudioEncoder audioEncoder;
  private AudioHEncode audioHEncode;
  private AudioRecord audioRecord;
  private RecordListener listener;
  /* recording runnable */
  private RecordingRunnable recordingRunnable=null;
  /* recording flag */
  private boolean recording;
  private int bufferSize = 0;
  /* write file for debug */
  private final boolean saved;
  private MediaWriter aacWriter;
  private MediaWriter wavWriter;

  public Microphone() {
    this(false);
  }

  public Microphone(boolean saved) {
    this(null, saved);
  }

  public Microphone(CodecFormat audioFormat) {
    this(audioFormat, false);
  }

  public Microphone(CodecFormat audioFormat, boolean saved) {
    if (audioFormat != null) {
      this.audioEncoder = new AudioEncoder(audioFormat);
    } else {
      this.audioEncoder = null;
    }

    this.saved = saved;

    audioHEncode=new AudioHEncode(audioFormat!=null);
  }

  //audioSource suggest use MediaRecorder.AudioSource.VOICE_COMMUNICATION
  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  public boolean start(int audioSource, int sampleRate, int channelCount) {
    return start(audioSource, sampleRate, channelCount, null);
  }

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  public boolean start(int audioSource, int sampleRate, int channelCount, RecordListener listener) {
    /*if (recordingRunnable == null) {
      synchronized (this) {
        if (recordingRunnable == null) {
          this.listener = listener;
          initAudioRecord(audioSource, sampleRate, channelCount);
          if (audioRecord == null) {
            return false;
          }

          if (audioEncoder != null) {

            audioEncoder.prepare(sampleRate, channelCount, BIT_RATE, this);
            if (audioEncoder.getState() != CodecState.PREPARED) {
              return false;
            }

            if (saved) {
              aacWriter = new MediaWriter("encode", ".aac");
            }
          } else {

            if (saved) {
              wavWriter = new MediaWriter("pcm", ".wav");
              try {
                wavWriter.writeWavHeader(sampleRate, channelCount == 1 ? AudioFormat.CHANNEL_IN_MONO
                    : AudioFormat.CHANNEL_IN_STEREO, AUDIO_ENCODING);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }

          recording = true;
          recordingRunnable = new RecordingRunnable(this);
          Thread thread = new Thread(recordingRunnable);
          thread.setPriority(Thread.MAX_PRIORITY);
          thread.start();
          return true;
        }
      }
    }else return false;*/
    if(audioHEncode.isAlive())
    {
      return false;
    }
    EncodeHListener listener2=new EncodeHListener()
    {
      private boolean isStartFirst=true;
      //private long priviousTime=talkTime;

      @Override
      public void onAudioEncode (final byte[] audiodata)
      {
        //long ttt=System.currentTimeMillis();
          isStartFirst=false;
          listener.onSpeaking( audiodata, audiodata.length);
        // Log.e("123", "sendAudio time=" + (ttt - talkTime)+",interval="+(ttt-priviousTime)+",send time="+(System.currentTimeMillis()-ttt));// + ",nDataSize=" + nDataSize + ",length=" + bpData.length);
        //priviousTime=ttt;

      }

      @Override
      public void onAudioStop () {
        EtxLogger.log(TAG, "onTurnOff----------------------------------------");
        listener.onTurnOff();
      }

      public boolean isStartFirst()
      {
        return isStartFirst;
      }


    };
    //Log.e("123","audiosource="+data.audiosource+",volume="+data.volume+",json="+json);
   //int audioSource=MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    EtxLogger.log(TAG, "startRecord---------------------------------------->");
    listener.onStartMic();
    audioHEncode.startRecord(listener2,audioSource,1, sampleRate,  channelCount);
    return true;
  }

  public void stop() {

         /* if (audioEncoder != null) {
            audioEncoder.setEos();
          } else if (saved) {
            try {
              wavWriter.close();
              wavWriter.updateWavHeader();
            } catch (Exception ignored) {
            }
          }
            recording = false;
          if(audioRecord!=null){
              audioRecord.stop();
              Log.i(TAG, "audio record stop");
              audioRecord.release();
              Log.i(TAG, "audio record release");
              audioRecord=null;
          }


          recordingRunnable = null;*/



    if(audioHEncode!=null)audioHEncode.setStopRecord();

  }

  //no use
  private void initAudioRecord(int audioSource, int sampleRate, int channelCount) {
    int channelConfig =
        channelCount == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
    int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AUDIO_ENCODING);
    bufferSize = Math.max(BYTES_2048, minBufferSize);
    try {
      audioRecord = getAudioRecord(audioSource, sampleRate, channelConfig, bufferSize);
      Log.i(TAG, "init audio record");
    } catch (IllegalArgumentException custom) {
      Log.i(TAG, "create audio record with buffer size failed, replace with min buffer size");
      bufferSize = minBufferSize;
      try {
        audioRecord = getAudioRecord(audioSource, sampleRate, channelConfig, bufferSize);
      } catch (IllegalArgumentException min) {
        Log.i(TAG, "create audio record with min buffer size failed");
      }
    } finally {
      if (audioRecord != null) {
        audioRecord.startRecording();
        Log.i(TAG, "audio record start recording");
      }
    }
  }

  private AudioRecord getAudioRecord(int audioSource, int sampleRate, int channelConfig, int bufferSize) {
    return new AudioRecord(audioSource, sampleRate, channelConfig, AUDIO_ENCODING, bufferSize);
  }

  private void record(byte[] buffer) {
    int bufferSize = buffer.length;
    int offset = 0;
    /* collect audio data until the buffer size */
    while (true) {
      if (!recording) {
        /* break when not recording */
        break;
      }

      int read = audioRecord.read(buffer, offset, bufferSize - offset);
      if (read > 0) {
        offset += read;
      } else {
        break;
      }

      if (offset >= bufferSize) {
        break;
      }
    }

    if (offset != AudioRecord.ERROR_INVALID_OPERATION) {
      if (audioEncoder != null) {
        audioEncoder.encode(buffer, offset, 0);
      } else if (listener != null) {
        listener.onSpeaking(buffer, offset);
        wavWriter.write(buffer);
      }
    }
  }

  @Override public void onEncode(byte[] chunk, int length) {
    if (listener != null) {
      listener.onSpeaking(chunk, length);
    }

    if (saved) {
      aacWriter.write(chunk);
    }
  }

  @Override public void onStop() {
    if (listener != null) {
      listener.onTurnOff();
    }

    if (saved) {
      aacWriter.close();
    }
  }

  private static class RecordingRunnable implements Runnable {

    private final Microphone instance;
    private final byte[] buffer;

    RecordingRunnable(Microphone reference) {
      instance = new WeakReference<>(reference).get();
      buffer = new byte[instance.bufferSize];
    }

    @Override public void run() {
      Log.i(TAG, "RECORDING -> start");
      while (instance.recording) {
        instance.record(buffer);
      }

      Log.i(TAG, "RECORDING -> stop");
    }
  }
}