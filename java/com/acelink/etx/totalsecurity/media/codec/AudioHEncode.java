package com.acelink.etx.totalsecurity.media.codec;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Environment;
import android.util.Log;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.media.codec.listener.EncodeHListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


/**
 * Created by Gregory on 2016/12/6.
 */
public class AudioHEncode {


    private int myByteRate=0;

    // Channel
    private  final int CHANNEL_COUNT = 1;

    // Codec
    private MediaCodec audioCodec;

    // Record
    private AudioRecord audioRecord;

    // Recording flag
    private boolean isRecording;

    // Encode listener
    private EncodeHListener encodeListener;

    private short[] shortBuffer;
    private static ByteArrayOutputStream byteOutputbuffer;
    private static int bufferCount;
    private Thread mThread;
    private NoiseSuppressor noise;

    private FileOutputStream aacOutputStream;
    private FileOutputStream aac2OutputStream;
    private FileOutputStream pcmOutputStream;

    private static boolean isLog=false;
    private static boolean isSaveAAC=isLog&&false;
    private static boolean isSaveAACAllPackage=isLog&&false;
    private static boolean isSavePCM=isLog&&false;
    private File savefile;
    private long t0=0;
    private Integer frameCount=0;
    private int audiosource= MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    private int volume=1;
    //private boolean isGain=false;
    private int sampleRate;
    private int channelCount;
    private int frequencyIndex;
    boolean isAAC=false;
    private ExecutorService newSingleThreadExecutor ;
   // private TimerUtil timer;

    public AudioHEncode(boolean isAAC){
        this.isAAC=isAAC;
    }

    public void startRecord(EncodeHListener encodeListener,int audiosource,int volume, int sampleRate, int channelCount) {
        this.encodeListener = encodeListener;
        this.audiosource=audiosource;
        this.volume=volume;
       this.sampleRate=sampleRate;
       this.channelCount=channelCount;
        this.frequencyIndex = CodecConstants.getFrequencyIndex(sampleRate);
        mThread=new Thread(recorderRunnable);
        mThread.setPriority(Thread.MAX_PRIORITY);
        mThread.start();
    }

    public boolean isAlive()
    {
        if(mThread==null)
            return false;
        return mThread.isAlive();
    }

    private void stopRecord()
    {
       // nullTimer();
        if(noise != null)
        {
            noise.release();
            noise=null;
        }
        if (audioRecord != null) {
            try {
            audioRecord.release();
            } catch(IllegalStateException ise) {}
            audioRecord = null;
        }

        if (audioCodec != null )
        {
            synchronized (audioCodec)
            {
                try {
                    audioCodec.stop();
                } catch(Exception ise) {}
                try {
                    audioCodec.release();
                }catch (Exception e){}
                audioCodec = null;
            }

        }
        if(byteOutputbuffer!=null)
        {
            try {
                byteOutputbuffer.close();
            }catch (Exception e){}

            byteOutputbuffer=null;
        }
        bufferCount=0;
        if(isSavePCM)
        {
            if(savefile!=null) {
                try {
                    updateWavHeader(savefile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(pcmOutputStream!=null)
            {
                try {
                    pcmOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        if(isSaveAAC)
        {
            if(aacOutputStream!=null) {
                try {
                    aacOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(isSaveAACAllPackage)
        {
            if(aac2OutputStream!=null) {
                try {
                    aac2OutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        isRecording = false;

    }

    public void setStopRecord()
    {
        EtxLogger.log(TAG, "Microphone setStopRecord");
        isRecording = false;
    }



    private void initAudioCodec(int sample) {
        EtxLogger.log(TAG, "init MIC H AudioCodec");
        if(byteOutputbuffer!=null)
        {
            try {
                byteOutputbuffer.close();
            }catch (Exception e){}

            byteOutputbuffer=null;
        }
        byteOutputbuffer=new ByteArrayOutputStream();
        //int myBitsPerSample= 2;
        //int myByteRate = SAMPLE_RATE * CHANNEL_COUNT * myBitsPerSample/8;
       // Log.e("123","myByteRate="+myByteRate);
        try {

            audioCodec = MediaCodec.createEncoderByType( MediaFormat.MIMETYPE_AUDIO_AAC);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME,  MediaFormat.MIMETYPE_AUDIO_AAC);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_BIT_RATE,16000);//11025);//, 64000);//VS1008 need use 48000
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);//for adts,defacut define VBR
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 6 * 1024);//作用於inputBuffer的大小
//            format.setInteger(MediaFormat.KEY_AAC_SBR_MODE,0);
            format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);//optimize

            audioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //Log.e("yoyo1", "initRecordCodec()");

            audioCodec.start();

        } catch (IOException e) {
            if(audioCodec!=null)
            {
                try {
                    audioCodec.release();
                }catch (Exception ee){}
                audioCodec=null;
            }
            e.printStackTrace();
        }

            String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
            if(isSavePCM){
                savefile = new File(filename, "honneywave" + ".wav");
                try {
                    pcmOutputStream=new FileOutputStream(savefile);
                    writeWavHeader(pcmOutputStream, AudioFormat.CHANNEL_IN_MONO, sampleRate, AudioFormat.ENCODING_PCM_16BIT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(isSaveAAC)
            {
                File file = new File(filename, "honneywave" + ".aac");
                try {
                    aacOutputStream=new FileOutputStream(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        if(isSaveAACAllPackage)
        {
            File file = new File(filename, "honneywaveallpack" + ".aac");
            try {
                aac2OutputStream=new FileOutputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private static void writeWavHeader(OutputStream out, int channelMask, int sampleRate, int encoding) throws IOException {
        short channels;
        switch (channelMask) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable channel mask");
        }

        short bitDepth;
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bitDepth = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitDepth = 16;
                break;
            case AudioFormat.ENCODING_PCM_FLOAT:
                bitDepth = 32;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable encoding");
        }

        writeWavHeader(out, channels, sampleRate, bitDepth);
    }

    private static void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }

    private static void updateWavHeader(File wav) throws Exception {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                        // There are probably a bunch of different/better ways to calculate
                        // these two given your circumstances. Cast should be safe since if the WAV is
                        // > 4 GB we've already made a terrible mistake.
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Subchunk2Size
                .array();

        RandomAccessFile accessWave = null;
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            // Rethrow but we still close accessWave in our finally
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }


    private Runnable recorderRunnable = new Runnable() {
        @Override
        public void run() {
            stopRecord();
            isRecording = true;
            initAudioCodec(sampleRate);
            recording3();
        }
    };


    private long preAudioTime=0;
    private int recordCount=0;//counts in input buffers
    @SuppressLint("MissingPermission")
    private void recording3()
    {
        int tempSize = 2048;

        byte[] zsbuffer = new byte[tempSize];
        t0=System.currentTimeMillis();
        preAudioTime=t0;
        frameCount=0;
        recordCount=0;
        newSingleThreadExecutor = Executors.newSingleThreadExecutor();

         newSingleThreadExecutor.execute(()->{
                       while (isRecording)   {
                          dequeueData2();
                       }
                   });


       // if(timer==null)timer=new TimerUtil();
        do {
            long startTime=System.currentTimeMillis();
            long interval=startTime-t0;
            t0=startTime;
            if(audioCodec==null)
            {

                    initAudioCodec(sampleRate);
            }
            if (audioRecord == null)
            {

                //int minSize=AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int minSize=AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if(minSize<tempSize)
                    minSize=tempSize;
                if(isLog)Log.e("123","init audioRecord minSize="+minSize+",audiosource="+audiosource+",volume="+volume);
                audioRecord = new AudioRecord(audiosource, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,minSize// SAMPLE_RATE*3//minSize
                );


                if(audioRecord.getState()==AudioRecord.STATE_UNINITIALIZED)
                {
                    minSize=AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    if(isLog)Log.e("123","init audioRecord fail min size="+AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
                    if (audioRecord != null) {
                        try {
                            audioRecord.release();
                        }catch (Exception e){}
                        audioRecord = null;
                    }
                    //tempSize=minSize;
                    audioRecord = new AudioRecord(audiosource, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, minSize
                    );
                }
                if(audioRecord.getState()==AudioRecord.STATE_INITIALIZED)
                {
                    //closeAESSupport(audioRecord.getAudioSessionId());
                   /* noise=NoiseSuppressor.create(audioRecord.getAudioSessionId());


                    if (noise!=null&&noise.getEnabled() == false)
                    {
                        noise.setEnabled(true);
                    }*/


                   // Log.e("123", "init audioRecord state=" + audioRecord.getState()+",noice enable="+(noise==null?false:noise.getEnabled()));
                }
                audioRecord.startRecording();

            }

            Arrays.fill(zsbuffer, (byte) 0);

            int nTotal = 0;


            do
            {
                int bufferReadResult = audioRecord.read(zsbuffer, nTotal, (tempSize - nTotal));

                if(bufferReadResult>0)
                {
                    nTotal += bufferReadResult;
                }else{
                    Log.e(TAG, "invalid  microphone recording---------------------------------------->"+bufferReadResult);
                    break;
                }


                if (nTotal == tempSize)
                {
                    break;
                }/*else
                {
                    if(isAAC&&isRecording){
                        EtxLogger.log(TAG, "encodeData2 nTotal---------------------------------------->"+nTotal);
                        encodeData2(null, false );
                    }

                }*/

            } while (isRecording);
            long recordTime=(System.currentTimeMillis()-t0);
            if(isLog)Log.e("123","audioRecord time="+recordTime+",interval="+interval);
            if(isRecording&&isSavePCM)
            {
                try {
                    pcmOutputStream.write(zsbuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] buffer = new byte[tempSize];
            /*if(isGain)
            {
                gainVoice(zsbuffer, buffer, nTotal,volume);
            }else
            {
                System.arraycopy(zsbuffer,0,buffer,0,tempSize);
            }*/
            System.arraycopy(zsbuffer,0,buffer,0,tempSize);
            //findMAX(zsbuffer);

             if(!isAAC)
             {//PCM
                 if (isRecording&&nTotal == tempSize)
                    encodeListener.onAudioEncode(buffer);
             }else
             {
                 if(audioRecord!=null&&nTotal == tempSize)
                 {
                     synchronized (audioRecord)
                     {

                        if (isRecording)
                        {
                            encodeData2(buffer, false );
                        } else
                            encodeData2(buffer, true);
                     }

                 }
             }
           /* if(!timer.isTiming()){
                timer.startTimer(new TimerUtil.TimerTaskCallBack() {
                    @Override
                    public void onActive() {
                        dequeueData2();
                    }
                },0,5);
            }   */

            if(isLog)Log.e("123","audioencode time="+(System.currentTimeMillis()-t0)+",decode interval="+(System.currentTimeMillis()-t0-recordTime));
        }while (isRecording);
        try {
            newSingleThreadExecutor.shutdown();
            newSingleThreadExecutor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //timer.removeMyTimer();
        synchronized (byteOutputbuffer)
        {

            if(isAAC&&bufferCount>0)
            {
                byte[] mData=byteOutputbuffer.toByteArray();
                encodeListener.onAudioEncode(mData);
                Log.e("123", "audio record end bufferCount="+bufferCount );
                if(isSaveAAC)
                {
                    try {
                        aacOutputStream.write(mData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bufferCount=0;
                byteOutputbuffer.reset();
            }
        }


        if(encodeListener!=null)encodeListener.onAudioStop();
        stopRecord();
    }

    private AcousticEchoCanceler canceler;
    public void closeAESSupport(int audioSession)
    {
        if(AcousticEchoCanceler.isAvailable())
        {

            canceler = AcousticEchoCanceler.create(audioSession);
            if(canceler!=null)canceler.setEnabled(false);
            Log.e("123","closeAESSupport="+(canceler!=null));
        }
    }


    private void findMAX(byte[] recBuffer)
    {
        float a=0;
        StringBuffer bur=new StringBuffer();
        for (int i=0;i<recBuffer.length*0.5;i++)
        {
            float sample = (float)( recBuffer[i  ] & 0xFF | recBuffer[i+1] << 8 );
            if(sample>a)
                a = sample;
            bur.append(sample);
            bur.append(',');
        }
        Log.e("123", "find max=" + a + ",bur="+bur);
    }



    private float[] bytesToFloats(byte[] bytes) {
        float[] floats = new float[bytes.length / 2];
        for(int i=0; i < bytes.length; i+=2) {
            floats[i/2] = bytes[i] | (bytes[i+1] < 128 ? (bytes[i+1] << 8) : ((bytes[i+1] - 256) << 8));
        }
        return floats;
    }

   /* private void gainVoice(byte[] recBuffer,byte[] recBuffer2 ,int reallySampledBytes,int gain ) {
        int n1, n2;
        int count=0;
        for (int i = 0; i < reallySampledBytes; i += 2) {

            n2 = (int) (((recBuffer[i] & 0xFF) | recBuffer[i + 1] << 8) );
            n1= (int) (n2*(1+0.1*gain));

            if (n1 < -0x7FFF) n1 = -0x7FFF;
            else if (n1 > 0x7FFF) {
                n1 = 0x7FFF;
                count++;
            }
            recBuffer2[i] = (byte) (((short) n1) & 0xff);
            recBuffer2[i + 1] = (byte) ((((short) n1) >> 8) & 0xff);
        }
        //Log.e("123","invalid sound="+count);
    }*/

    private void amp2(short[] recBuffer, short[] buffer, int reallySampledBytes, int lFactor)
    {
        int y;

        for (int i = 0; i < reallySampledBytes; i++) {
            y = (int)((((long)recBuffer[i] * lFactor + 0x80)) >> 8);
            if (y < -0x7FFF) y= -0x7FFF; else if (y > 0x7FFF) y= 0x7FFF;
            buffer[i] = (short)y;
        }
    }

    private void amp2(byte[] recBuffer, byte[] buffer ,int reallySampledBytes,long lFactor)
    {

        int count = reallySampledBytes / 2;
       // short[] audioData = new short[count];
        int y;

        for (int i = 0; i < count; i++) {
            /*short MSB = (short) recBuffer[2 * i + 1];
            short LSB = (short) recBuffer[2 * i];
            short shorti = (short) (MSB << 8 | (255 & LSB));*/
            //short n1 = (short)((recBuffer[i*2] & 0xFF) | recBuffer[i*2 + 1] << 8);
            //y = (int)(((long)n1 * lFactor + (0x80)) >> 8);
            short n1 = (short)((recBuffer[i * 2] & 0xFF) | recBuffer[i * 2 + 1] << 8);
           // y = n1 + 1000;
            //y = (int)(((long)n1 * lFactor + (0x80)) >> 8);
            y = (short)(n1 << 2);
            //if (y < -0x7FFF) y= -0x7FFF; else if (y > 0x7FFF) y = 0x7FFF;
            short xx=(short)y;;
           // buffer[2*i+1] = (byte)(xx >>> 0);
            //buffer[2*i] = (byte)(xx >>> 8);
            buffer[2*i] = (byte)(xx & 0xff);
            buffer[2*i + 1] = (byte)((xx >> 8) & 0xff);
        }


           /* short *sample = (short *)buf;
             if(count)
            do {
                y = ((long)*sample++ * lFactor + 0x80) >> 8;
                if (y<-0x7FFF) y=-0x7FFF; else if (y>0x7FFF) y=0x7FFF;
                sample[-1] = (signed short)y;
            } while(--count);
        */
    }



    private synchronized void encodeData2(byte[] array, boolean end)
    {
        int inIndex=-1;

        if(array!=null)
        {
            ByteBuffer[] inputBuffers = audioCodec.getInputBuffers();


            while((inIndex = audioCodec.dequeueInputBuffer(-1)) < 0&&isRecording ) { // -1: wait indefinitely for the buffer
                switch(inIndex) {
                    default:
                        Log.e("123", "recordAudio dequeueInputBuffer returned unknown value: " + inIndex);
                }
            }

            //int inIndex = audioCodec.dequeueInputBuffer(-1);
            if (inIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inIndex];

                inputBuffer.clear();
                inputBuffer.put(array);
                if(!end)
                {
                    audioCodec.queueInputBuffer(inIndex, 0, array.length, 0, 0);
                    recordCount++;
                }
                else {
                    audioCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int outIndex = audioCodec.dequeueOutputBuffer(info, 0);
                    if(isLog)Log.e("123", "BUFFER_FLAG_END_OF_STREAM outIndex="+outIndex +",flag="+info.flags);
                    if(outIndex >= 0)
                    {
                        audioCodec.releaseOutputBuffer(outIndex, false);

                    }
                    dequeueData2();
                    return;
                }
            }else {
                if(isLog)Log.e("123", "encodeData2 <0------------------------------------> " + inIndex);
            }
        }
        //dequeueData2();

    }

   @SuppressLint("WrongConstant")
   private void dequeueData2(){
       MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
       int outIndex =-1;
       if(isRecording)
           outIndex =audioCodec.dequeueOutputBuffer(info, 0);//10000);
     //  if(isLog)Log.e("123", "encodeData2 outIndex: " + outIndex+",frequencyIndex="+frequencyIndex+",size="+info.size);
       while (audioCodec!=null&&outIndex >= 0&&isRecording)
       {

           int outBuffSize = info.size;
           ByteBuffer outBuffer = audioCodec.getOutputBuffers()[outIndex];
            /*if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && info.size != 0) {
                if(isLog)Log.e("123", "dequeueOutputBuffer flags: " + info.flags);
                audioCodec.releaseOutputBuffer(outIndex, false);
                break;
            }*/

           if (outBuffSize > 10 && isRecording) { /* Skip first two bytes */
               frameCount++;
               final byte[] chunk = new byte[info.size + 7];
               DecodeUtils.addADTStoPacket(chunk, chunk.length,frequencyIndex,channelCount);//, SAMPLE_RATE, CHANNGEL_COUNT);
               outBuffer.position(info.offset);
               outBuffer.limit(outBuffSize + info.offset);
               outBuffer.get(chunk, 7, info.size);
              // final byte[] chunk = new byte[info.size + 8];
               //chunk[7]= frameCount.byteValue();
              // outBuffer.get(chunk, 8, info.size);
               outBuffer.clear();

               if (isRecording && encodeListener != null) {
                   synchronized (byteOutputbuffer) {
                       try {
                           byteOutputbuffer.write(chunk);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       if (isRecording && isSaveAACAllPackage) {
                           try {
                               aac2OutputStream.write(chunk);
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                       recordCount--;
                       bufferCount++;
                      // if (isLog) Log.e("123", "bufferCount: " + bufferCount + ",recordCount=" + recordCount + ",outIndex=" + outIndex+",bytesCounts="+byteOutputbuffer.size());
                        if(bufferCount>=1){
                            //if ((encodeListener.isStartFirst()&&bufferCount >= (sampleRate>=16000?16:8))||!encodeListener.isStartFirst()&&bufferCount >= 1) {
                           byte[] mData = byteOutputbuffer.toByteArray();
                           long tt=System.currentTimeMillis();
                            if (isLog) Log.e("123", "frameCount: " + frameCount + ",mData isze=" + mData.length);
                           encodeListener.onAudioEncode(mData);
                           if (isLog){
                               long writeTime=System.currentTimeMillis()-tt;
                               Log.e("123", "send data: " + outIndex + ",size=" + mData.length+",writeTime="+writeTime + ",period=" + (System.currentTimeMillis() - preAudioTime) + ",recordCount=" + recordCount);

                           }

                           preAudioTime=System.currentTimeMillis();
                           if (isRecording && isSaveAAC) {
                               try {
                                   aacOutputStream.write(mData);
                               } catch (IOException e) {
                                   e.printStackTrace();
                               }
                           }

                           bufferCount = 0;
                           byteOutputbuffer.reset();
                       }


                   }
                   //encodeListener.onAudioEncode(chunk);


               }
               //if(isLog)Log.e("123", "encodeData2 inIndex="+inIndex+", releaseout: " + outIndex+",length="+chunk.length);
           }
           try {
               audioCodec.releaseOutputBuffer(outIndex, false);
               info = new MediaCodec.BufferInfo();
               outIndex = audioCodec.dequeueOutputBuffer(info, 0);
           } catch (IllegalStateException e) {
               outIndex = -1;
               Log.w("123", "AudioEncode releaseOutputBuffer IllegalStateException: ");
           }

           //outIndex =audioCodec.dequeueOutputBuffer(info, 0);

       }
   }
}
