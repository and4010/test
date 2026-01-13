package com.acelink.etx.totalsecurity.aggregate.codec;

import static com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode.Dewrap_1O;
import static com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode.Dewrap_1O_to_Wall_1R;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.acelink.etx.EtxLogger;

import com.acelink.etx.totalsecurity.EdimaxSecurityGuard;
import com.acelink.etx.totalsecurity.FrameFPS;
import com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode;
import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type;
import com.acelink.etx.totalsecurity.defish.DeFish1280;
import com.acelink.etx.totalsecurity.defish.DeFish1280Ceiling;
import com.acelink.etx.totalsecurity.defish.DeFish1944;
import com.acelink.etx.totalsecurity.defish.DeFish1944Ceiling;
import com.acelink.etx.totalsecurity.defish.DeFishWall;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.codec.CodecState;
import com.acelink.etx.totalsecurity.media.codec.MediaFormatBuilder;

import com.ns.greg.library.fancy_logger.FancyLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AggregateVideoDecoder extends AggregateBaseCodec {

  public interface LiveDoubleClickCallbackListener {
    void onLiveDoubleClick();
  }

  private Dewrap_mode dewrap_mode=Dewrap_mode.Dewrap_1O;
  /* init defines, do not modify */
  private static final String TAG = "AggregateVideoDecoder";

  private TextureView mTextureView;
  /* rendered surface */
  private Surface surface;
  /* video CSD-0 data */
  private byte[] csd0;
  private int csd0Size;
  /* video resolution */
  private static final int DEFAULT_WIDTH = 1944;
  private static final int DEFAULT_HEIGHT = 1944;

  private LiveDoubleClickCallbackListener            m_myCallback = null;
  private int bitmapWIDTH = 0;//test
  private int bitmapHEIGHT = 0;//test

  private int surfaceWIDTH = 0;
  private int surfaceHEIGHT = 0;
  private int surfaceImageWIDTH = 0;
  private int surfaceImageHEIGHT = 0;
  private int max_scale = 5;

  private float min_scale = 1f;
  private float normal_scale_portrait = 1.5f;
  private float premove = 0f;
  private float move = 0f;
  private float mscale = 1f;
  private int transX=0,transY=0;

  private int currentFrameCount=0;

  private long currentBytesReceives=0L;
  private long codecInitTime=0;

  private FrameFPS fps= FrameFPS.FPS20;

  private Mount_type mountType= Mount_type.WallOrDesk;

  private DeFish1280 deFish1280=new DeFish1280();

  private DeFish1944 deFish1944=new DeFish1944();

  private DeFish1280Ceiling deFish1280Ceiling=new DeFish1280Ceiling();

  private DeFish1944Ceiling deFish1944Ceiling=new DeFish1944Ceiling();

  private DeFishWall deFishWall=new DeFishWall();
  private AtomicBoolean needFlush=new AtomicBoolean(false);

  public AggregateVideoDecoder(CodecFormat codecFormat) {
    super(codecFormat);
  }

  /*--------------------------------
   * Rendering function
   *-------------------------------*/

  public void setFPS(FrameFPS f){
    this.fps=f;
  }

  public void setSnapShotImage(ImageView snapshot){
    this.img1OTo1P1R=snapshot;
  }

  public void unVisableSnapshot(){
    if (dewrap_mode==Dewrap_mode.Dewrap_1P||dewrap_mode==Dewrap_mode.Dewrap_1O)
    {
      if (img1OTo1P1R != null) {
        img1OTo1P1R.post(() -> {
          try {
            img1OTo1P1R.setVisibility(View.GONE);//hide snapshot
          } catch (Exception e) {

          }

        });
      }
    }
  }

  public void setTextureView(TextureView textureView) {
    if(mTextureView!=null)mTextureView.removeOnLayoutChangeListener(layoutChangeListener);
    this.mTextureView = textureView;
    surface=null;
    initTextureView();
  }

  private void initTextureView(){
    mTextureView.setOnTouchListener(imageTouch);
    mTextureView.removeOnLayoutChangeListener(layoutChangeListener);
    mTextureView.addOnLayoutChangeListener(layoutChangeListener);
    if (mTextureView.getHeight() != 0 && mTextureView.getWidth() != 0) {
      surfaceWIDTH = mTextureView.getWidth();
      surfaceHEIGHT = mTextureView.getHeight();
      updateMatrix(1, 0, 0,true);
    }
  }

  private View.OnLayoutChangeListener layoutChangeListener= new View.OnLayoutChangeListener() {
    @Override
    public void onLayoutChange (View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      if (v.getHeight() != 0 && v.getWidth() != 0) {
        if(surfaceWIDTH!=v.getWidth()){
          surfaceWIDTH = v.getWidth();
          surfaceHEIGHT = v.getHeight();
          EtxLogger.log(TAG, getClass().getSimpleName(),"onLayoutChange surfaceWIDTH=" + surfaceWIDTH + ",surfaceHEIGHT=" + surfaceHEIGHT);
          initTransValue();
          updateMatrix(1, 0, 0,true);
        }
      }
    }
  };

  public TextureView getTextureView() {
    return mTextureView;
  }

  /*--------------------------------
   * Codec functions
   *-------------------------------*/

  public void prepare(byte[] csd0, int csd0Size) {
    this.csd0 = csd0;
    this.csd0Size = csd0Size;
   // FancyLogger.e("234","prepare AggregateVideoDecoder csd0="+Arrays.toString(csd0));
    prepare();
  }

  protected void prepare() {
    synchronized (this) {
      setState(CodecState.PREPARING);
      initMediaFormat();
      initCodec();
    }
  }

  @Override protected void initMediaFormat() {
    initVideoFormat(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  private void initVideoFormat(int w,int h){
    MediaFormatBuilder builder= MediaFormatBuilder.videoFormat(getMimeType(), w, h);
    builder.setByteBuffer(CSD_0, ByteBuffer.wrap(csd0, 0, csd0Size));
    if(fps== FrameFPS.FPS20){
      builder.setMaxInputSize(DEFAULT_WIDTH* DEFAULT_HEIGHT*4);
      builder.enableLowLatency();
    }else {
      builder.setMaxInputSize(1280* 960*4);
    }
    builder .colorSurface();

      setFormat(builder.build());
    EtxLogger.logE(TAG, getClass().getSimpleName(), "INIT VIDEO FORMAT -> succeeded");
  }

  @Override protected void initCodec() {
    surface = new Surface(mTextureView.getSurfaceTexture());
    if(surface==null){
      EtxLogger.logE(TAG, "INIT Video CODEC -> failed, surface is null");
      return;
    }
    configureCodec();
  }


  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private static String selectCodec(String mimeType) {
    MediaCodecList codecLists = new MediaCodecList(MediaCodecList.ALL_CODECS);
    MediaCodecInfo[] infos = codecLists.getCodecInfos();
    ArrayList<MediaCodecInfo> C2LowLatency=new ArrayList<MediaCodecInfo>();//maybe I decode together too
    ArrayList<MediaCodecInfo> lowLatency=new ArrayList<MediaCodecInfo>();//maybe I decode together too
    ArrayList<MediaCodecInfo> C2List=new ArrayList<MediaCodecInfo>();
    ArrayList<MediaCodecInfo> H264List=new ArrayList<MediaCodecInfo>();
    ArrayList<MediaCodecInfo> hardwareList=new ArrayList<MediaCodecInfo>();
    ArrayList<MediaCodecInfo> softwareList=new ArrayList<MediaCodecInfo>();
    EtxLogger.logE(TAG, "selectCodec", "selectCodec length"+infos.length);
    mediaInfo:
    for (int i = 0; i < infos.length; i++) {
      MediaCodecInfo codecInfo = infos[i];

      if (codecInfo.isEncoder())
      {
        continue;
      }

      String[] types = codecInfo.getSupportedTypes();

      for (int j = 0; j < types.length; j++) {
        if (types[j].equalsIgnoreCase(mimeType)) {

          MediaCodecInfo.CodecCapabilities caps=codecInfo.getCapabilitiesForType(mimeType);
          MediaCodecInfo.VideoCapabilities vcap = caps.getVideoCapabilities();
          String name=codecInfo.getName().toLowerCase();
          boolean failCodec=(name.indexOf("OMX.Exynos.".toLowerCase()) >=0||name.contains(".secure"));
          //boolean isHardwareEncode =!failCodec&& (codecInfo.getName().toLowerCase().indexOf("omx.google") == -1)&&(codecInfo.getName().toLowerCase().indexOf("sw")==-1);

          boolean isSoftware=!failCodec&&( (codecInfo.getName().toLowerCase().startsWith("omx.google"))||(codecInfo.getName().toLowerCase().indexOf("sw") >=0));//OMX:OPENMAX/ACodec，
          boolean isPreferDecode =(!failCodec&& !isSoftware);
          boolean accel=Build.VERSION.SDK_INT >Build.VERSION_CODES.Q? codecInfo.isHardwareAccelerated():false;
          boolean ccodec=isPreferDecode&&name.startsWith("c2.")&&!name.startsWith("c2.android") ;//C2:CCodec, c2.exynos。lantency also worse,
          boolean low_latency=(name.indexOf("low_latency".toLowerCase()) >=0)&&isPreferDecode;
          boolean supportHigh=false;
          for(MediaCodecInfo.CodecProfileLevel level:caps.profileLevels){
            if (level.profile ==MediaCodecInfo.CodecProfileLevel.AVCProfileHigh){
              supportHigh=true;
            }
          }

          if(isPreferDecode||low_latency||ccodec){

            EtxLogger.logE(TAG, "selectCodec", "selectCodec First select name = "+codecInfo.getName()+",maxWidth="+vcap.getSupportedWidths().getUpper()+",supportHigh="+supportHigh+",ccodec="+ccodec+",low_latency="+low_latency+",isHardwareEncode="+isPreferDecode
                    +",isHardwareAccelerated="+accel//+" colorfmats"+Arrays.toString(caps.colorFormats)+",profiles="+Arrays.toString(caps.profileLevels)
            );
            if(ccodec&&low_latency){
              C2LowLatency.add(codecInfo);
            }
            else if(low_latency){
              lowLatency.add(codecInfo);
            }else  if(ccodec){
              if(supportHigh)
                C2List.add(0,codecInfo);
              else {
                C2List.add(codecInfo);
              }
            }
             else if(accel){
              H264List.add(codecInfo);
            }else if(isPreferDecode) {
              hardwareList.add(codecInfo);
            }
            continue mediaInfo;
           // return codecInfo;
          }else if(isSoftware){
            EtxLogger.logE(TAG, "selectCodec", "selectCodec Software select name = "+codecInfo.getName());
            softwareList.add(codecInfo);
           // continue mediaInfo;
          }else {
            EtxLogger.logE(TAG, "selectCodec", "not Select codec name -> "+codecInfo.getName());
          }
        }
      }

    }
    EtxLogger.logE(TAG, "selectCodec", "H264List size="+((H264List.size())) +" hardwareList size="+hardwareList.size()+" softwareList size="+softwareList.size());
    if(C2LowLatency.size()>0)
    {
      return C2LowLatency.get(0).getName();
    }
    if(C2List.size()>0)
    {
      return C2List.get(0).getName();
    }
    if(lowLatency.size()>0)
    {
      return lowLatency.get(0).getName();
    }
    if(hardwareList.size()>0){
      return hardwareList.get(0).getName();
    }
    if(H264List.size()>0){
      return H264List.get(0).getName();
    }


    if(softwareList.size()>0){
      return softwareList.get(0).getName();
    }

    return null;
  }

  private void configureCodec(){
    try {

      //MediaCodec codec = MediaCodec.createDecoderByType(getMimeType());
      var name=selectCodec(getMimeType());
      MediaCodec codec = MediaCodec.createByCodecName(name);
      EtxLogger.logE(TAG, "selectCodec", "selectCodec createByCodecName name="+name+" ok");

      codec.configure(getFormat(), surface, null, 0);
      setCodec(codec);
      startCodec();
    } catch (IllegalArgumentException e) {
      //setState(CodecState.FAILED);
      e.printStackTrace();
      surface.release();

      EtxLogger.logE(TAG, "INIT AggregateVideoDecoder -> failed, IllegalArgumentException");
      initCodec();
    }catch (Exception e) {
      setState(CodecState.FAILED);
      e.printStackTrace();
      EtxLogger.logE(TAG, "INIT AggregateVideoDecoder -> failed, exception");
    }
  }

  /**
   * Decode the raw video data
   *
   * @param content video data
   * @param lengtht length of video data
   * /* @param format video format
   * @param iFrame is i-frame flag
   * /* @param width source width
   * /* @param height source height
   * /* @param playTimeMs play time ms
   */

  private Queue<VideoFrame> frames=new LinkedList <VideoFrame>();//no use
  public void decode(byte[] content, int lengtht, int iFrame,long playTimeMs/*, TsRtpFormat.VideoFormat forma*/)
  {

    if(iFrame== EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_VSPPS)return;

    currentFrameCount++;//above VSPPS will greater 20
    currentBytesReceives+=lengtht;
    int interval =(int)(1000*((double)currentFrameCount/(fps==FrameFPS.FPS20?20:30)));
    long currentTime=Calendar.getInstance().getTimeInMillis();
     long actualInterval  =currentTime -codecInitTime;
    /*synchronized (frames){
      boolean insert=frames.offer(new VideoFrame(content,lengtht,iFrame));
      if (!insert)Log.e("123","insert fail currentFrameCount="+currentFrameCount);
    }*/
    queneInput(content,lengtht,iFrame);
    //FancyLogger.e("123","decode actualInterval="+(actualInterval/1000f)+" latency ="+(currentTime-playTimeMs)+",kbps="+(currentBytesReceives/(double)actualInterval)+",receive real devf="+(actualInterval-interval)+" frame rate="+(currentFrameCount/((actualInterval/1000f))) +" real frame="+(fps==FrameFPS.FPS20?20:30));

  }

  private void queneInput(byte[] content, int lengtht, int iFrame){
    if (content != null && lengtht > 0) {
      if(iFrame>2&&needFlush.compareAndSet(true,false)){
        synchronized (getCodec()){
          EtxLogger.log(TAG, getClass().getSimpleName(), "decode video needFlush flush==========>,iFrame="+iFrame);
          getCodec().flush();
        }
      }
      // EtxLogger.log(TAG, getClass().getSimpleName(), "decode video ======> iFrame="+iFrame+",CodecState="+getState());
      int inputBufferIndex = getCodec().dequeueInputBuffer(TIMEOUT);
      //if(iFrame>=2)
      //  EtxLogger.log(TAG, getClass().getSimpleName(), "decode video ======> inputBufferIndex="+inputBufferIndex+",iFrame="+iFrame);

      if (inputBufferIndex >= 0) {
        if (isEos()) {
          getCodec().queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        } else
        {
          ByteBuffer inputBuffer;
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            inputBuffer = getCodec().getInputBuffers()[inputBufferIndex];
            inputBuffer.clear();
          } else {
            inputBuffer = getCodec().getInputBuffer(inputBufferIndex);
          }

          if (inputBuffer != null) {
            if(iFrame== EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_I) {
              //  EtxLogger.logE(TAG, getClass().getSimpleName(), "AggregateVideoDecoder BUFFER_FLAG_KEY_FRAME data 5="+content[4]);
            }
            inputBuffer.put(content, 0, lengtht);
            getCodec().queueInputBuffer(inputBufferIndex, 0, lengtht, 0L,  0);
          }
        }
      }else {
        EtxLogger.logE(TAG, getClass().getSimpleName(), "AggregateVideoDecoder Need FLUSH xxxxxxxxx inputBufferIndex="+inputBufferIndex+"=============================================>");

        needFlush.set(true);
        frames.clear();
      }
    }
  }

  @Override void process() {
/*
    VideoFrame f=null;
    synchronized (frames){
        f= frames.poll();
    }
    if (f!=null) queneInput(f.content,f.lengtht,f.iFrame);
    */
  }

  private int previousOutPutIndex=-1;
  @Override void output() {
     boolean sameIndex=false;
    try {
      MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
      int outputBufferIndex = getCodec().dequeueOutputBuffer(bufferInfo, TIMEOUT);
    //  EtxLogger.log(TAG, getClass().getSimpleName(), "decode video outputBufferIndex="+outputBufferIndex);
      switch (outputBufferIndex) {
        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:// ignored, since using surface view
          break;

        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:// TODO: should considering this when format changed?
          EtxLogger.logE(TAG, getClass().getSimpleName(), getClass().getSimpleName()+" INFO_OUTPUT_FORMAT_CHANGED width="+getRawDataWidth()+",height="+getRawDataHeight());
          MediaFormat outputFormat = getCodec().getOutputFormat();
          //Log.e(LogTab,"mine tye="+outputFormat.getString(outputFormat.KEY_MIME));
          final int bwidth = outputFormat.getInteger(MediaFormat.KEY_WIDTH);
          final int bheight = outputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        //  EtxLogger.logE(TAG, getClass().getSimpleName(), "change bwidth="+bwidth+",bheight="+bheight+" change outputFormat=" +outputFormat);
          EtxLogger.logE( getClass().getSimpleName(), "INFO_OUTPUT_FORMAT_CHANGED bwidth="+bwidth+",bheight="+bheight+" change outputFormat=" +outputFormat);
          final int viewWidth = mTextureView.getWidth();
          final int viewHeight = mTextureView.getHeight();
          bitmapWIDTH = bwidth;
          bitmapHEIGHT = bheight;
          frames.clear();
          if ((viewWidth != 0 && viewHeight != 0 && bwidth != 0 && bheight != 0)) {
            surfaceWIDTH = viewWidth;
            surfaceHEIGHT = viewHeight;
            updateMatrix(1, 0, 0,true);
            isSetting1OTo1PImage.set(false);
            isSetting4RImage.set(false);
            isSetting1OToR2Image.set(false);
            initFrameRate();
            if (dewrap_mode==Dewrap_mode.Dewrap_1P//||dewrap_mode==Dewrap_mode.Dewrap_1O
            )
            {
              if (img1OTo1P1R != null) {
                img1OTo1P1R.post(() -> {
                  img1OTo1P1R.setVisibility(View.GONE);//hide snapshot
                });
              }
            }
           if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R){
              EtxLogger.logE( getClass().getSimpleName(), "INFO_OUTPUT_FORMAT_CHANGED Dewrap_1O_to_4R mountType="+mountType);
              if(img4R1!=null&&img4R2!=null&&img4R3!=null&&img4R4!=null) {
                EtxLogger.logE( getClass().getSimpleName(), "INFO_OUTPUT_FORMAT_CHANGED Dewrap_1O_to_4R image  all not null");
                init1Oto4RMatrix(img4R1,img4R2,img4R3,img4R4);
              }

            }else if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R2){
              if(R2Touch!=null){
                R2Touch.initMatrix();
              }
            }else if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P){
              if(img1OTo1P1R!=null){
                int w=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();
                int h=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();
                img1OTo1P1R.setScaleType(ImageView.ScaleType.MATRIX);
                EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_Wall_1P output bitmapWIDTH="+bitmapWIDTH+",dewrap w="+w+"================>"+"mountType="+mountType);
                img1OTo1P1R.setOnTouchListener(new Sub1OTo1PTouch(img1OTo1P1R,w,h));
              }
              updateMatrix(1,0,0,true);

            }else {
              if (img1OTo1P1R!=null){
                new Thread(()->{
                    try {
                        Thread.sleep(100);//avoid flashing
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (img1OTo1P1R!=null){
                    img1OTo1P1R.post(()->{
                      img1OTo1P1R.setVisibility(View.GONE);
                    });
                  }
                }).start();

              }
            }
          }

          int color =outputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
          EtxLogger.logE(TAG, getClass().getSimpleName(), "change KEY_COLOR_FORMAT ="+color);

          break;

        case MediaCodec.INFO_TRY_AGAIN_LATER:
        //  Log.e("123","INFO_TRY_AGAIN_LATER outPutTimePos="+outPutTimePos);
          break;

        default:

          synchronized (surface){
            getCodec().releaseOutputBuffer(outputBufferIndex, true);
          }
          sameIndex=previousOutPutIndex==outputBufferIndex;
          previousOutPutIndex=outputBufferIndex;
         // Log.e("123","releaseOutputBuffer outputBufferIndex="+outputBufferIndex);



          set1OTo1P4RImageView();
          set1OTo1R2ImageView();


          break;
      }


    } catch (Exception ignored) {
    //  ignored.printStackTrace();
      EtxLogger.logE("123",  "END OF AggregateVideoDecoder output Exception isEos="+isEos());
    }finally {
      if (isEos()) {
        // EtxLogger.logE(TAG, getClass().getSimpleName(),  "MEET FLAG -> `END OF AggregateVideoDecoder STREAM` State="+getState());

        stopCodec();
        releaseCodec();
        mTextureView=null;
        EtxLogger.logE("123",  "MEET FLAG -> `END OF AggregateVideoDecoder STREAM` State="+getState());
      }
    }

    try {
      if(!isEos()){
        //>26 will latercy become long
        if(fps==FrameFPS.FPS30){
          if(previousOutPutIndex>=0)Thread.sleep(30);//>=32 pixel4 sometime crash
          else Thread.sleep(14);
        }else {
          if(previousOutPutIndex>=0)Thread.sleep(30);//35//>=40 s22 lag
          else Thread.sleep(17);
        }

      }

    } catch (Exception e) {e.printStackTrace();}
  }



  private AtomicBoolean isSetting4RImage=new AtomicBoolean(false);



  private AtomicBoolean isSetting1OTo1PImage=new AtomicBoolean(false);
  private Bitmap bitmap1OTo1P;
  private void set1OTo1P4RImageView(){

    if((dewrap_mode==Dewrap_mode.Dewrap_1O_to_1P||dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R||dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R))
    {
      EtxLogger.log(TAG, getClass().getSimpleName(), "set1OTo1P4RImageView dewrap_mode ="+dewrap_mode+", mountType="+mountType+",isSetting1OTo1PImage="+isSetting1OTo1PImage);
      if(isSetting1OTo1PImage.compareAndSet(false,true))
      {
        if(img1OTo1P1R==null&&(dewrap_mode==Dewrap_mode.Dewrap_1O_to_1P||dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R)){
          EtxLogger.log(TAG,"123","set1OTo1P4RImageView img1OTo1P1R is null");
          isSetting1OTo1PImage.set(false);
          return;
        }
        if(img4R1==null&&(dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R)){
          isSetting1OTo1PImage.set(false);
          EtxLogger.log(TAG,"set1OTo1P4RImageView img4R1 is null");
          return;
        }
        Thread tt=new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              int width=getRawDataWidth();
              int height=getRawDataHeight();
              if (height!=width)height=width;// arrowsF-01K will only 1920*1088
              Bitmap map=mTextureView.getBitmap(width, height);
              Bitmap map1P=null;
              //EtxLogger.log(TAG,"set1OTo1PImageView fisheye map width="+map.getWidth()+",height="+map.getHeight());
              if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P)
              {
                if(Math.abs(map.getWidth())-1280<=10){
                  map1P=deFishWall.deFish12801P(map);
                }else if(Math.abs(map.getWidth())-1944<=30){
                  map1P=deFishWall.deFish19441P(map);
                //  if (map1P!=null)Log.e("map1P","map1P w="+map1P.getWidth());
                }
              }else //Dewrap_1O_to_1P Dewrap_1O_to_1R Dewrap_1O_to_4R
              {
                if(Math.abs(map.getWidth())-1280<=10){
                  if(mountType==Mount_type.Ceiling){
                    map1P=deFish1280Ceiling.deFish1280(map);
                  }else if(mountType==Mount_type.WallOrDesk)
                  {
                    map1P=deFishWall.deFish12801P(map);
                  }else
                  {
                    map1P=deFish1280.deFish1280(map);
                  }

                }else if(Math.abs(map.getWidth())-1944<=30)
                {
                  if(mountType==Mount_type.Ceiling){
                    map1P=deFish1944Ceiling.deFish1944(map);
                  }else if(mountType==Mount_type.WallOrDesk){
                    map1P=deFishWall.deFish19441P(map);
                  }else {
                    map1P=deFish1944.deFish1944(map);
                  }
                }
              }

              map.recycle();
              if(map1P!=null){
                final Bitmap recycleMap=bitmap1OTo1P;//old map recycle after new map set

                bitmap1OTo1P=map1P;
                mTextureView.post(new Runnable() {
                  @Override
                  public void run() {
                    try {
                      if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R)
                      {
                        synchronized (img4R1){
                          img4R1.setImageBitmap(bitmap1OTo1P);
                          img4R2.setImageBitmap(bitmap1OTo1P);
                          img4R3.setImageBitmap(bitmap1OTo1P);
                          img4R4.setImageBitmap(bitmap1OTo1P);
                        }
                      }else
                      {
                        synchronized (img1OTo1P1R){
                          img1OTo1P1R.setImageBitmap(bitmap1OTo1P);
                        }
                      }

                      if(recycleMap!=null)recycleMap.recycle();
                    }catch (Exception e){
                      e.printStackTrace();
                      StringWriter sw = new StringWriter();
                      e.printStackTrace(new PrintWriter(sw));
                      String exceptionAsString = sw.toString();
                      EtxLogger.log(TAG,"set1OTo1P4RImageView Exception msg="+exceptionAsString);
                    } finally {
                      isSetting1OTo1PImage.set(false);
                    }
                  }
                });
              }else {
                EtxLogger.log(TAG,"set1OTo1PImageView map1P is null");
                isSetting1OTo1PImage.set(false);
              }

            }catch (Exception e){
              e.printStackTrace();
              isSetting1OTo1PImage.set(false);
              StringWriter sw = new StringWriter();
              e.printStackTrace(new PrintWriter(sw));
              String exceptionAsString = sw.toString();
              EtxLogger.log(TAG,"set1OTo1P4RImageView Exception2 msg="+exceptionAsString);
            }

          }
        });
        tt.setPriority(Thread.MAX_PRIORITY);
        tt.start();
      }
    }
  }

  private AtomicBoolean isSetting1OToR2Image=new AtomicBoolean(false);

  private long getPreviousRTime=0;
  private void set1OTo1R2ImageView(){
    if((dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R2)
            &&isSetting1OToR2Image.compareAndSet(false,true))
    {
      if(img1OTo1P1R==null&&(dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R2)){
        Log.e("123","set1OTo1R2ImageView img1OTo1P1R is null");
        isSetting1OToR2Image.set(false);
        return;
      }

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            long to=System.currentTimeMillis();
           // Log.e("123","set1OTo1R2ImageView get interval<============ end "+(to-getPreviousRTime));
            getPreviousRTime=to;
            int width=getRawDataWidth();
            int height=getRawDataHeight();
            if (height!=width)height=width;// arrowsF-01K will only 1920*1088
            Bitmap map=mTextureView.getBitmap(width, height);
            EtxLogger.log(TAG,"set1OTo1R2ImageView map width="+map.getWidth()+",height="+map.getHeight());
            if(map!=null){

              try {
                R2Touch.setMap1O(map);

                R2Touch.setImageBitmap();
             //   Log.e("123","set1OTo1R2ImageView get setMap1O end "+(System.currentTimeMillis()-to));
                isSetting1OToR2Image.set(false);
              }catch (Exception e){
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                EtxLogger.log(TAG,"set1OTo1R2ImageView Exception R2 1 msg="+exceptionAsString);
              } finally {
                isSetting1OToR2Image.set(false);
              }
            }
         // Log.e("123","set1OTo1R2ImageView end=========================> "+(System.currentTimeMillis()-to));
          }catch (Exception e){
            e.printStackTrace();
            isSetting1OToR2Image.set(false);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            EtxLogger.log(TAG,"set1OTo1R2ImageView Exception R2 2 msg="+exceptionAsString);
          }
        }
      }).start();
    }
  }

  private void initFrameRate(){
    currentFrameCount=0;
    currentBytesReceives=0;
    codecInitTime=Calendar.getInstance().getTimeInMillis();
  }

  public float getFrameRealRate(){
    long currentTime=Calendar.getInstance().getTimeInMillis();
    long actualInterval  =currentTime -codecInitTime;
    try {
     return  (currentFrameCount/((actualInterval/1000f)));
    }catch (Exception e){
      e.printStackTrace();
    }
    return -1f;
  }

  public long getReceiveRealFrameDiff(){
    //int interval =(int)(1000*((double)currentFrameCount/(fps==FrameFPS.FPS20?20:30)));
    long currentTime=Calendar.getInstance().getTimeInMillis();
    long actualInterval  =currentTime -codecInitTime;
    float frame=(fps==FrameFPS.FPS20?20:30)*actualInterval/1000f;
  //  return  actualInterval-interval;
    return (int)frame-currentFrameCount;
  }

  @Override public void startCodec() throws NullPointerException {
    if (isState(CodecState.PREPARING) || isState(CodecState.STOP)) {
      try {
        needFlush.set(false);
        initFrameRate();
        setState(CodecState.PREPARED);
        super.startCodec();

        Log.e(TAG, "AggregateVideoDecoder START CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "AggregateVideoDecoder START CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "AggregateVideoDecoder START CODEC -> failed, not configured");
      }
    } else {
      Log.e(TAG, "AggregateVideoDecoder START CODEC -> failed, illegal state "+getState());
    }
  }

  @Override void stopCodec() {
    /* FIXME: should considering preparing */
    if (isState(CodecState.PREPARED)) {
      try {
        setState(CodecState.STOP);
        initFrameRate();
        codecInitTime=0;
        super.stopCodec();
        Log.i(TAG, "STOP AggregateVideoDecoder -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "STOP AggregateVideoDecoder -> failed, no instance");
      } catch (MediaCodec.CodecException e){
        e.printStackTrace();
        Log.e(TAG, "STOP AggregateVideoDecoder -> CodecException failed");
      }
      catch (IllegalStateException e) {
        e.printStackTrace();
        Log.e(TAG, "STOP AggregateVideoDecoder -> failed, is in release state");
      }
    } else {
      Log.e(TAG, "STOP AggregateVideoDecoder -> failed, illegal state");
    }
  }

  @Override void releaseCodec() {
    if (isState(CodecState.STOP)) {
      try {
        setState(CodecState.RELEASE);
        super.releaseCodec();
        if (surface != null) {
          surface.release();
        }
        Log.e(TAG,"RELEASE AggregateVideoDecoder -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "RELEASE AggregateVideoDecoder -> failed, no instance");
      }
      surface=null;
    } else {
      Log.e(TAG,"RELEASE AggregateVideoDecoder -> failed, illegal state");
    }
  }

  public void switchTextureView(TextureView textureView) {
    if (isState(CodecState.PREPARED)) {
      synchronized (this){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          setTextureView(textureView);
          surface = new Surface(mTextureView.getSurfaceTexture());
          getCodec().setOutputSurface(surface);

        } else {
          stopCodec();
          releaseCodec();
          mTextureView=null;
          setTextureView(textureView);
          prepare();
        }
      }

    } else {
      Log.e(TAG, "SWITCH TARGET VIEW -> failed, state is not prepared");
    }
  }

  @SuppressLint("SuspiciousIndentation")
  public int getRawDataWidth() {
    if (getCodec() == null || getCodec().getOutputFormat() == null)
      return 0;
    else
    return getCodec().getOutputFormat().getInteger(MediaFormat.KEY_WIDTH);
  }

  public int getRawDataHeight() {
    if (getCodec() == null || getCodec().getOutputFormat() == null)
      return 0;
    else
      return getCodec().getOutputFormat().getInteger(MediaFormat.KEY_HEIGHT);
  }

  public Dewrap_mode getDewrapMode(){
   return dewrap_mode;
  }

  public void setMountType(Mount_type type){
    FancyLogger.e("AggregateVideoDecoder","AggregateVideoDecoder setMountType="+type);
    this.mountType=type;
    Sub1OTo1R2Touch.mountType=type;
    Sub4RTouch.mountType=type;
    if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R){

    }
  }

  public void setDewrapMode1O(){
    if(dewrap_mode== Dewrap_1O)return;
    max_scale=5;
    dewrap_mode= Dewrap_1O;
    mscale=1;
    setOtherImageViewGone();
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O=================================>");
    updateMatrix(1,0,0,true);
  }

  public void setDewrapMode1OToWall1R(){
    if(dewrap_mode== Dewrap_1O_to_Wall_1R)return;
    max_scale=5;
    dewrap_mode= Dewrap_1O_to_Wall_1R;
    mscale=2.8f;
    setOtherImageViewGone();
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_Wall_1R=================================>");
    updateMatrix(1,0,0,true);
  }

  public void setDewrapMode1P(){
    if(dewrap_mode==Dewrap_mode.Dewrap_1P)return;
    max_scale=5;
    dewrap_mode=Dewrap_mode.Dewrap_1P;
    mscale=1;
    setOtherImageViewGone();
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1P=================================>");
    updateMatrix(1,0,0,true);
  }



  private ImageView img4R1;
  private ImageView img4R2;
  private ImageView img4R3;
  private ImageView img4R4;



  private ImageView img1OTo1P1R;
  public void setDewrapMode1OTo1P(ImageView img1OTo1P1R){
    // if(dewrap_mode==Dewrap_mode.Dewrap_4R&&img1==img4R1)return;
    dewrap_mode=Dewrap_mode.Dewrap_1O_to_1P;
    max_scale=5;
    mscale=1;
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_1P=================================>"+"mountType="+mountType);
    setOtherImageViewGone();
    img1OTo1P1R.setImageResource(android.R.color.black);
    img1OTo1P1R.setVisibility(View.VISIBLE);

    this.img1OTo1P1R =img1OTo1P1R;
    updateMatrix(1,0,0,true);
        int w=Math.abs(bitmapWIDTH-1280)<=10?1457:2217;
    int h=Math.abs(bitmapWIDTH-1280)<=10?465:707;
    img1OTo1P1R.setScaleType(ImageView.ScaleType.MATRIX);
    img1OTo1P1R.setOnTouchListener(new Sub1OTo1PTouch(img1OTo1P1R,w,h));
  }

  public void setDewrapMode1OToWall1P(ImageView img1OTo1P1R){
    // if(dewrap_mode==Dewrap_mode.Dewrap_4R&&img1==img4R1)return;
    dewrap_mode=Dewrap_mode.Dewrap_1O_to_Wall_1P;
    max_scale=5;
    mscale=1;

    setOtherImageViewGone();
    img1OTo1P1R.setImageResource(android.R.color.black);
    img1OTo1P1R.setVisibility(View.VISIBLE);
    this.img1OTo1P1R =img1OTo1P1R;

    updateMatrix(1,0,0,true);

    int w=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();
    int h=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_Wall_1P bitmapWIDTH="+bitmapWIDTH+",dewrap w="+w+"================>"+"mountType="+mountType);
    img1OTo1P1R.setScaleType(ImageView.ScaleType.MATRIX);
    img1OTo1P1R.setOnTouchListener(new Sub1OTo1PTouch(img1OTo1P1R,w,h));

  }

  public void setDewrapMode1OTo1R(ImageView img1OTo1P1R){
    // if(dewrap_mode==Dewrap_mode.Dewrap_4R&&img1==img4R1)return;
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode setDewrapMode1OTo1R=================================>");
    dewrap_mode=Dewrap_mode.Dewrap_1O_to_1R;
    max_scale=5;
    mscale=1;
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode1OTo1R ");
    setOtherImageViewGone();
    img1OTo1P1R.setImageResource(android.R.color.black);
    img1OTo1P1R.setVisibility(View.VISIBLE);

    //WeakReference<ImageView> weakReference = new WeakReference<>(img1);
    this.img1OTo1P1R =img1OTo1P1R; //weakReference.get();

    updateMatrix(1,0,0,true);
    //    int w=bitmapWIDTH==1280?1947:2957;
//    int h=bitmapWIDTH==1280?470:714;
    int w=Math.abs(bitmapWIDTH-1280)<=10?1457:2217;
    int h=Math.abs(bitmapWIDTH-1280)<=10?465:707;
    img1OTo1P1R.setScaleType(ImageView.ScaleType.MATRIX);
    img1OTo1P1R.setOnTouchListener(new Sub1OTo1RTouch(img1OTo1P1R,w,h));

  }

  private Sub1OTo1R2Touch R2Touch;
  public void setDewrapMode1OTo1R2(ImageView img1OTo1P1R){
    // if(dewrap_mode==Dewrap_mode.Dewrap_4R&&img1==img4R1)return;

    dewrap_mode=Dewrap_mode.Dewrap_1O_to_1R2;
    max_scale=5;
    mscale=1;
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_1R2  bitmapWIDTH="+bitmapWIDTH+"=================================>"+"mountType="+mountType);

    setOtherImageViewGone();
    img1OTo1P1R.setImageResource(android.R.color.black);
    img1OTo1P1R.setVisibility(View.VISIBLE);

    //WeakReference<ImageView> weakReference = new WeakReference<>(img1);
    this.img1OTo1P1R =img1OTo1P1R; //weakReference.get();

    updateMatrix(1,0,0,true);
    int w=Math.abs(bitmapWIDTH-1280)<=20?1457:2217;
    int h=Math.abs(bitmapWIDTH-1280)<=20?465:707;
//    int w=200;
//    int h=200;
    img1OTo1P1R.setScaleType(ImageView.ScaleType.FIT_XY);
    R2Touch=(new Sub1OTo1R2Touch(img1OTo1P1R,w,h));
    img1OTo1P1R.setOnTouchListener(R2Touch);

  }

  public void setDewrapMode1OTo4R(ImageView img1,ImageView img2,ImageView img3,ImageView img4){
    // if(dewrap_mode==Dewrap_mode.Dewrap_4R&&img1==img4R1)return;

    dewrap_mode=Dewrap_mode.Dewrap_1O_to_4R;
    max_scale=5;
    mscale=1;
    EtxLogger.logE(TAG, getClass().getSimpleName(), "setDewrapMode Dewrap_1O_to_4R  bitmapWIDTH="+bitmapWIDTH+"=================================>"+"mountType="+mountType);
    setOtherImageViewGone();
    if(this.img4R1!=null)img4R1.setImageResource(android.R.color.transparent);
    if(this.img4R2!=null)img4R2.setImageResource(android.R.color.transparent);
    if(this.img4R3!=null)img4R3.setImageResource(android.R.color.transparent);
    if(this.img4R4!=null)img4R4.setImageResource(android.R.color.transparent);
    img1.setImageResource(android.R.color.black);
    img2.setImageResource(android.R.color.black);
    img3.setImageResource(android.R.color.black);
    img4.setImageResource(android.R.color.black);
    img1.setVisibility(View.VISIBLE);
    img2.setVisibility(View.VISIBLE);
    img3.setVisibility(View.VISIBLE);
    img4.setVisibility(View.VISIBLE);

    this.img4R1 =img1;
    this.img4R2 =img2;
    this.img4R3 =img3;
    this.img4R4 =img4;
    updateMatrix(1,0,0,true);
    //int w=bitmapWIDTH==1280?1947:2957;
   // int h=bitmapWIDTH==1280?470:714;
    init1Oto4RMatrix(img1,img2,img3,img4);
  }

  private void init1Oto4RMatrix(ImageView img1,ImageView img2,ImageView img3,ImageView img4){
    img1.setScaleType(ImageView.ScaleType.MATRIX);
    img2.setScaleType(ImageView.ScaleType.MATRIX);
    img3.setScaleType(ImageView.ScaleType.MATRIX);
    img4.setScaleType(ImageView.ScaleType.MATRIX);

    if(mountType==Mount_type.Ceiling){
      int w=Math.abs(bitmapWIDTH-1280)<=10?1457:2217;
      int h=Math.abs(bitmapWIDTH-1280)<=10?465:707;
      img1.setOnTouchListener(new Sub4RTouch(img1,w,h,0));
      img2.setOnTouchListener(new Sub4RTouch(img2,w,h,1));
      img3.setOnTouchListener(new Sub4RTouch(img3,w,h,2));
      img4.setOnTouchListener(new Sub4RTouch(img4,w,h,3));
    }else if (mountType==Mount_type.WallOrDesk)
    {
      int w=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();
      int h=Math.abs(bitmapWIDTH-1280)<=10?DeFishWall.Companion.getBitmap1280WallWIDTH():DeFishWall.Companion.getBitmap1944WallWIDTH();

      img1.setOnTouchListener(new Sub4RTouch(img1,w,h,0));
      img2.setOnTouchListener(new Sub4RTouch(img2,w,h,1));
      img3.setOnTouchListener(new Sub4RTouch(img3,w,h,2));
      img4.setOnTouchListener(new Sub4RTouch(img4,w,h,3));
    }
  }

  private void setOtherImageViewGone(){
    if(mTextureView!=null){
      mTextureView.post(new Runnable() {

        @Override
        public void run() {
          if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R)
          {
            if(img1OTo1P1R!=null)img1OTo1P1R.setVisibility(View.GONE);

          }else if(dewrap_mode==Dewrap_mode.Dewrap_1O_to_1P
                  ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R
                  ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R2
                  ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P){
            if(img4R1!=null)img4R1.setVisibility(View.GONE);
            if(img4R2!=null)img4R2.setVisibility(View.GONE);
            if(img4R3!=null)img4R3.setVisibility(View.GONE);
            if(img4R4!=null)img4R4.setVisibility(View.GONE);
          }else {
            if(img4R1!=null)img4R1.setVisibility(View.GONE);
            if(img4R2!=null)img4R2.setVisibility(View.GONE);
            if(img4R3!=null)img4R3.setVisibility(View.GONE);
            if(img4R4!=null)img4R4.setVisibility(View.GONE);
            if(img1OTo1P1R!=null)img1OTo1P1R.setVisibility(View.GONE);
          }

        }
      });
    }
  }

  private void initTransValue()
  {
    move = 0f;
    mscale=1;
    transX=0;
    transY=0;
   /* if(dewrap_mode==Dewrap_mode.Dewrap_1R){
      mscale=4;
    }else {*/
      mscale=1;
   // }
  }

  public void setMaxScale (int scale) throws ArithmeticException
  {
    if(scale>=1&&scale<=8)
    {
      this.max_scale=scale;
      if(mscale>max_scale)
      {
        mscale=max_scale;
        updateMatrix(mscale,0,0,true);
      }
    }
    else throw new ArithmeticException("scale value must between 1~8");
  }

  private View.OnTouchListener imageTouch = new View.OnTouchListener() {
    /**
     * 0x0000, init
     */
    private int MODE_INIT = 0x0000;

    /**
     * 0x0001, touch down
     */
    private int MODE_DOWN = 0x0001;

    /**
     * 0x0010, pointer down
     */
    private int MODE_POINTER_DOWN = 0x0010;

    /**
     * 0x0010, up
     */
    private int MODE_POINTER_UP = 0x0100;

    /**
     * 0x0010, pointer up
     */
    private int MODE_UP = 0x1000;

    private boolean isMove = false;

    private boolean isDrag = false;

    private float down_x = 0.0f, down_y = 0.0f, up_x = 0.0f, up_y = 0.0f;

    private int mode = 0;

    private float pre_Dist = 0.0f;

    private boolean hasOneClick = false;

    private float pre_scale = 0f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

      if(mTextureView ==null)return false;
      switch (event.getAction() & MotionEvent.ACTION_MASK)
      {
        case MotionEvent.ACTION_DOWN:

          isDrag = false;

          isMove = false;

          mode = MODE_DOWN;

          down_x = event.getX();

          down_y = event.getY();

          break;

        case MotionEvent.ACTION_POINTER_DOWN:

          mode += MODE_POINTER_DOWN;

          pre_Dist = spacing(event);

          break;

        case MotionEvent.ACTION_UP:
          if(!isDrag)
            mode += MODE_UP;

          up_x = event.getX();

          up_y = event.getY();

          move = ((Math.abs(down_x - up_x)) + (Math.abs(down_y - up_y)));
          if( move < 75 )
          {
            isMove = false;
            if(mode<MODE_UP)mode += MODE_UP;
          }
          break;

        case MotionEvent.ACTION_POINTER_UP:

          mode += MODE_POINTER_UP;

          break;

        case MotionEvent.ACTION_MOVE:
          move = ((Math.abs(down_x - event.getX())) + (Math.abs(down_y - event.getY())));
          if(premove != move)
            premove = move;
          if( move > 75)
          {
            isMove = true;
          } else
          {
            if(premove != move)
            {
              isMove = false;
              premove = 0f;
            }
          }
          break;
      }
      // click


      if (!isMove &&  (mode == (MODE_UP + MODE_DOWN))) {
        mTextureView.removeCallbacks(doubleclick_run);
        //_controll.b_showbar = !_controll.b_showbar;
        if(m_myCallback!=null)m_myCallback.onLiveDoubleClick();
        if(dewrap_mode!=Dewrap_1O_to_Wall_1R){
          if (hasOneClick)
          {
            hasOneClick = false;

            doubleClick(event);
            return true;
          } else
          {
            if (bitmapWIDTH!=bitmapHEIGHT)//fisheye no double click function
            {
              /*mTextureView.postDelayed(doubleclick_run, 400);
              hasOneClick = true;//already Deprecated this function
              */
            }

          }
        }
      }
       //Log.e("123","isMove="+isMove+",move="+move+",premove="+premove+",isDrag="+isDrag);
      if ((isMove && premove == move) && mode == MODE_DOWN && mode != (MODE_UP + MODE_DOWN) )
      {

        if (mscale <= min_scale||mscale>max_scale)
          return true;
        float limitFactor=0.5f;
        if(dewrap_mode==Dewrap_1O_to_Wall_1R){
           limitFactor=0.13f;
          if(surfaceWIDTH>surfaceHEIGHT*1.4){
            limitFactor=0.08f;
          }
          Log.e("122","Dewrap_1O_to_Wall_1R limitFactor="+limitFactor);
        }

       // Log.e("123","mscale="+mscale+",min_scale="+min_scale+",max_scale="+max_scale+",normal_scale_portrait="+normal_scale_portrait);
        if ( (mscale <= normal_scale_portrait))
        {
          int xx=transX+(int)((event.getX() - down_x));
          if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*limitFactor&&xx>-1*limitFactor*(mscale*surfaceImageWIDTH-surfaceWIDTH))
          {
            transX=xx;
            updateMatrix(mscale, (int) (transX), 0,false);
          }

          // Log.e("123","ViewWidth="+surfaceWIDTH+",surfaceImageWIDTH="+surfaceImageWIDTH+",mscale="+mscale+",transX="+transX+",delta="+(mscale*surfaceImageWIDTH-surfaceWIDTH));
        }  else
        {
          int xx=transX+(int)((event.getX() - down_x));
          int yy=transY+(int)((event.getY() - down_y));
          if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*limitFactor&&xx>-1*limitFactor*(mscale*surfaceImageWIDTH-surfaceWIDTH))
            transX=xx;
          if(yy<limitFactor*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*limitFactor*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))
            transY=yy;
      //    Log.e("123", "ViewHeight=" + surfaceHEIGHT + ",surfaceImageWIDTH=" + surfaceImageHEIGHT + ",mscale=" + mscale + ",transY=" + transY + ",delta=" + (mscale*surfaceImageHEIGHT-surfaceHEIGHT));
          updateMatrix(mscale, (int) (transX), (int) (transY),false);


        }

        down_x = event.getX();
        down_y = event.getY();
        isDrag = true;


      }else if ( mode == (MODE_DOWN + MODE_POINTER_DOWN))
      {
       if( dewrap_mode!=Dewrap_1O_to_Wall_1R){
         if( event.getPointerCount()>=2)
         {
           float newDist = spacing(event);
           zoom(newDist / pre_Dist, down_x, down_y);
           pre_Dist = newDist;
         }
       }



      }
      return true;
    }

    private void zoom(float f, float x, float y)
    {

      float original_s_w = min_scale;

      float new_s_w = f;


      if (mscale * f > max_scale || mscale * f > max_scale)
      {
        return;
      } else if (mscale* f <= original_s_w|| mscale * f <= original_s_w)
      {

        mscale=min_scale;
        updateMatrix(mscale, 0, 0,false);
      } else
      {
        mscale=mscale*f;


        if(mscale*surfaceImageWIDTH<surfaceWIDTH)
        {
          transX=0;
        }else if(transX*f>(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5)
          transX=(int)((mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5);
        else if(transX*f<-1*(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5)
          transX=-1*(int)((mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5);
        else
          transX=(int)(transX*f);

        if(mscale*surfaceImageHEIGHT<surfaceHEIGHT)
        {
          transY=0;
        }else if(transY*f>(mscale*surfaceImageHEIGHT-surfaceHEIGHT)*0.5)
          transY=(int)((mscale*surfaceImageHEIGHT-surfaceHEIGHT)*0.5);
        else if(transY*f<-1*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)*0.5)
          transY=-1*(int)((mscale*surfaceImageHEIGHT-surfaceHEIGHT)*0.5);
        transY=(int)(transY*f);
        updateMatrix(mscale,(int)(transX), (int)(transY),false);

      }

    }


    Runnable doubleclick_run = new Runnable() {

      @Override
      public void run() {
        hasOneClick = false;
      }
    };

    private void doubleClick(MotionEvent event)
    {

        FancyLogger.e("doubleClick","doubleClick happen=========================================>");
      if(normal_scale_portrait<max_scale)
        {
          if (mscale == min_scale)
          {
            initTransValue();
            mscale=normal_scale_portrait;
            updateMatrix(mscale,0,0,true);
          } else
          {
            initTransValue();
            mscale=min_scale;
            updateMatrix(min_scale, 0, 0,true);
          }
        }else
        {
          if (mscale == min_scale)
          {
            initTransValue();
            mscale=max_scale;
            updateMatrix(mscale,0,0,true);
          } else
          {
            initTransValue();
            mscale=min_scale;
            updateMatrix(min_scale, 0, 0,true);
          }
        }

    }

    private float spacing(MotionEvent event)
    {
      float x = event.getX(0) - event.getX(1);
      float y = event.getY(0) - event.getY(1);
      return (float)Math.sqrt(x * x + y * y);
    }

  };

  private void updateMatrix(float scale,int Tx,int Ty,boolean init)
  {

    if(!isState(CodecState.PREPARED)|| mTextureView ==null||bitmapWIDTH==0||bitmapHEIGHT==0||surfaceHEIGHT==0||surfaceWIDTH==0){
      return;
    }
    if(dewrap_mode==Dewrap_mode.Dewrap_1P

            ||dewrap_mode== Dewrap_1O
            ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1P
            ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R
            ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_1R2
            ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_4R
            ||dewrap_mode==Dewrap_mode.Dewrap_1O_to_Wall_1P
            ||dewrap_mode== Dewrap_1O_to_Wall_1R
    )
    {
      updateMatrix1O(scale,Tx,Ty,init);
    }/*else  if(dewrap_mode==Dewrap_mode.Dewrap_1R){
      updateMatrix1R(4f,Tx,Ty);
    }*/


    //mSurface.getSurfaceTexture().updateTexImage();

  }

  private void updateMatrix1O(float scale,int Tx,int Ty,boolean isInit){
    int width = mTextureView.getWidth();
    int height = mTextureView.getHeight();
    float ratioSurface = (float) width / height;
    float ratioPreview = (float) bitmapWIDTH / bitmapHEIGHT;

    float scaleX;
    float scaleY;

    if (ratioSurface > ratioPreview)
    {
      scaleX = ((float) height /bitmapHEIGHT)*((float) bitmapWIDTH /width);
      scaleY = 1;
      normal_scale_portrait=1/scaleX;
    }
    else
    {
      scaleX = 1;
      scaleY = (float) width / bitmapWIDTH*((float) bitmapHEIGHT /height);
      normal_scale_portrait=1/scaleY;
    }
    if(isInit){
      FancyLogger.e("123","isInit  true dewrap_mode="+dewrap_mode);
      if(dewrap_mode==Dewrap_1O_to_Wall_1R){
        if(surfaceWIDTH>surfaceHEIGHT*1.5)//landscape
        {
          mscale=4.8f;
          scale=4.8f;
          FancyLogger.e("123","Dewrap_1O_to_Wall_1R init bitmapWIDTH="+bitmapWIDTH+",bitmapHEIGHT="+bitmapHEIGHT+",width="+width+",surfaceWIDTH="+surfaceWIDTH+",scaleX="+scaleX+",scaleY="+scaleY);
          transX= (int) (bitmapWIDTH*0.07f);
          transY= (int) (bitmapHEIGHT*0.05f);
          Tx=transX;
          Ty=transY;
        }else {
          mscale=3.7f;
          scale=3.7f;
          transX= (int) (bitmapWIDTH*0.08f);
          transY= (int) (bitmapHEIGHT*0.03f);
          Tx=transX;
          Ty=transY;
        }
        max_scale=6;
      }else //if(dewrap_mode==Dewrap_1O)
      {
        if (scale>3){
          mscale=3f;
          scale=1f;
        }

        max_scale = 5;
      }
    }


    final Matrix matrix = new Matrix();
    surfaceImageWIDTH=(int)(width * scaleX);
    surfaceImageHEIGHT=(int)(height * scaleY);
    matrix.setScale(scaleX*scale, scaleY*scale);
    float scaledWidth = width * scaleX*scale;
    float scaledHeight = height * scaleY*scale;
    float dx = (width - scaledWidth) / 2;
    float dy = (height - scaledHeight) / 2;
    matrix.postTranslate(dx + Tx, dy + Ty);
    //if(dx + Tx>)
    mTextureView.post(new Runnable() {
      @Override
      public void run () {
        try
        {
          synchronized (mTextureView)
          {
            mTextureView.setTransform(matrix);
            mTextureView.invalidate();
          }
        }catch (Exception e){}
      }
    });
   // EtxLogger.logE(TAG, getClass().getSimpleName(), "updateMatrix bW=" + bitmapWIDTH + ",bH=" + bitmapHEIGHT + ",width=" + (scaleX*width)+ ",height=" + (scaleY*height));
  }

  private void updateMatrix1R(float scale,int Tx,int Ty){
    int width = mTextureView.getWidth();
    int height = mTextureView.getHeight();
    float ratioSurface = (float) width / height;
    float ratioPreview = (float) bitmapWIDTH / bitmapHEIGHT;

    float scaleX;
    float scaleY;

    if (ratioSurface > ratioPreview)
    {
      scaleX = ((float) height /bitmapHEIGHT)*((float) bitmapWIDTH /width);
      scaleY = 1;
      normal_scale_portrait=1/scaleX;
    }
    else
    {
      scaleX = 1;
      scaleY = (float) width / bitmapWIDTH*((float) bitmapHEIGHT /height);
      normal_scale_portrait=1/scaleY;
    }

    final Matrix matrix = new Matrix();
    surfaceImageWIDTH=(int)(width * scaleX);
    surfaceImageHEIGHT=(int)(height * scaleY);
    matrix.setScale(scaleX*scale, scaleY*scale);
    float scaledWidth = width * scaleX*scale;
    float scaledHeight = height * scaleY*scale;
    float dx = (width - scaledWidth) / 2;
    float dy = (height - scaledHeight) / 2;//to center
    matrix.postTranslate(dx + Tx, dy + Ty);
    //if(dx + Tx>)
    mTextureView.post(new Runnable() {
      @Override
      public void run () {
        try
        {
          synchronized (mTextureView)
          {
            mTextureView.setTransform(matrix);
            mTextureView.invalidate();
          }
        }catch (Exception e){}
      }
    });
   // EtxLogger.logE(TAG, getClass().getSimpleName(), "updateMatrix bW=" + bitmapWIDTH  + ",bH=" + bitmapHEIGHT +",scale="+scale+ ",width=" + surfaceImageWIDTH+ ",height=" + surfaceImageHEIGHT);
  }

}