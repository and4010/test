package com.acelink.etx.totalsecurity.media.codec;

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

import androidx.annotation.RequiresApi;

import com.acelink.etx.EtxLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gregho
 * @since 2018/10/22
 */
public class VideoDecoder extends BaseCodec {

  public interface LiveDoubleClickCallbackListener {

    void onLiveDoubleClick();
  }
  /* init defines, do not modify */
  private static final String TAG = "VideoDecoder";

  private TextureView mTextureView;
  /* rendered surface */
  private Surface surface;
  /* video CSD-0 data */
  private byte[] csd0;
  private int csd0Size;
  /* video resolution */
  private static final int DEFAULT_WIDTH = 1280;
  private static final int DEFAULT_HEIGHT = 720;

  private LiveDoubleClickCallbackListener            m_myCallback = null;
  private int bitmapWIDTH = 0;//test
  private int bitmapHEIGHT = 0;//test
  private int surfaceWIDTH = 0;
  private int surfaceHEIGHT = 0;
  private int surfaceImageWIDTH = 0;
  private int surfaceImageHEIGHT = 0;
  private int max_scale = 3;

  private float min_scale = 1f;
  private float normal_scale_portrait = 1.5f;
  private float premove = 0f;
  private float move = 0f;
  private float mscale = 1f;
  private int transX=0,transY=0;
  private AtomicBoolean needFlush=new AtomicBoolean(false);

  public VideoDecoder(CodecFormat codecFormat) {
    super(codecFormat);
  }

  /*--------------------------------
   * Rendering function
   *-------------------------------*/

  public void setTextureView(TextureView textureView) {
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
      updateMatrix(1, 0, 0);
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

          updateMatrix(1, 0, 0);
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
    setFormat(MediaFormatBuilder.videoFormat(getMimeType(), w, h)
            .setByteBuffer(CSD_0, ByteBuffer.wrap(csd0, 0, csd0Size))
            .setMaxInputSize(DEFAULT_WIDTH* DEFAULT_HEIGHT*4)
            .build());
    EtxLogger.logE(TAG, getClass().getSimpleName(), "INIT VIDEO FORMAT -> succeeded");
  }

  @Override protected void initCodec() {
    surface = new Surface(mTextureView.getSurfaceTexture());
    if(surface==null){
      Log.e(TAG, "INIT Video CODEC -> failed, surface is null");
      return;
    }
    configureCodec();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private static MediaCodecInfo selectCodec(String mimeType) {

    MediaCodecInfo[] infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
    for (int i = 0; i < infos.length; i++) {
      MediaCodecInfo codecInfo = infos[i];

     // if (!codecInfo.isEncoder())
      if (codecInfo.isEncoder())
      {
        continue;
      }

      String[] types = codecInfo.getSupportedTypes();
      for (int j = 0; j < types.length; j++) {
        if (types[j].equalsIgnoreCase(mimeType)) {
          boolean isHardwareEncode = (codecInfo.getName().toLowerCase().indexOf("omx.google") == -1)&&(codecInfo.getName().toLowerCase().indexOf("sw")==-1);
          boolean failCodec=(codecInfo.getName().toLowerCase().indexOf("OMX.Exynos.avc.dec".toLowerCase()) >=0);
          if(isHardwareEncode&&!failCodec){
            MediaCodecInfo.CodecCapabilities caps=codecInfo.getCapabilitiesForType(mimeType);

            EtxLogger.logE(TAG, "selectCodec", "selectCodec select name = "+codecInfo.getName()+",isHardwareEncode="+isHardwareEncode+" colorfmats"+Arrays.toString(caps.colorFormats));

            return codecInfo;
          }else {
            EtxLogger.logE(TAG, "selectCodec", "not hardware codec name -> "+codecInfo.getName());
          }
        }
      }

      for (int j = 0; j < types.length; j++) {
        if (types[j].equalsIgnoreCase(mimeType)) {
          boolean isHardwareEncode = (codecInfo.getName().toLowerCase().indexOf("omx.google") == -1)&&(codecInfo.getName().toLowerCase().indexOf("sw")==-1);
         //selsec software codec
          if(!isHardwareEncode){
            MediaCodecInfo.CodecCapabilities caps=codecInfo.getCapabilitiesForType(mimeType);

            EtxLogger.logE(TAG, "selectCodec", "selectCodec software name = "+codecInfo.getName()+" colorfmats"+Arrays.toString(caps.colorFormats));

            return codecInfo;
          }else {
            EtxLogger.logE(TAG, "selectCodec", "hardware or fa codec name -> "+codecInfo.getName());
          }
        }
      }
    }




    return null;
  }

  private void configureCodec(){
    try {
    //  MediaCodec codec = MediaCodec.createDecoderByType(getMimeType());
      MediaCodec codec = MediaCodec.createByCodecName(selectCodec(getMimeType()).getName());
      codec.configure(getFormat(), surface, null, 0);
      setCodec(codec);
      startCodec();
    } catch (IOException e) {
      setState(CodecState.FAILED);
      e.printStackTrace();
      Log.e(TAG, "INIT CODEC -> failed, I/O exception");
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
  private long previousVTime=0;

  public void decode(byte[] content, int lengtht, int iFrame/*, TsRtpFormat.VideoFormat forma,long playTimeMs*/)
  {
    synchronized (this) {
     // setContent(content);
      //setContentLength(contentLength);
      if (content != null && lengtht > 0) {
        if(iFrame<2&&needFlush.compareAndSet(true,false)){
          synchronized (getCodec()){
            getCodec().flush();
          }
        }
        int inputBufferIndex = getCodec().dequeueInputBuffer(TIMEOUT);

      //  EtxLogger.log(TAG, getClass().getSimpleName(), "decode video inputBufferIndex="+inputBufferIndex);
        long now=System.currentTimeMillis();
        EtxLogger.log(TAG, getClass().getSimpleName(), "decode video inteval="+(now-previousVTime) +",content lengtht="+lengtht +",iFrame="+iFrame);
        previousVTime=now;

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
              inputBuffer.put(content, 0, lengtht);
              getCodec().queueInputBuffer(inputBufferIndex, 0, lengtht, 0L, 0);
            }
          }
        }else {

          //  MediaFormat outputFormat = getCodec().getOutputFormat();
           // initVideoFormat(outputFormat.getInteger(MediaFormat.KEY_WIDTH),outputFormat.getInteger(MediaFormat.KEY_HEIGHT));
           // configureCodec();
          needFlush.set(true);

        }
      }
    }
  }

  @Override void process() {}

  @Override void output() {
    try {
      MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
      int outputBufferIndex = getCodec().dequeueOutputBuffer(bufferInfo, TIMEOUT);
    //  EtxLogger.log(TAG, getClass().getSimpleName(), "decode video outputBufferIndex="+outputBufferIndex);
      switch (outputBufferIndex) {
        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
          /* ignored, since using surface view */
          break;

        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
          /* TODO: should considering this when format changed? */
          EtxLogger.logE(TAG, getClass().getSimpleName(), getClass().getSimpleName()+" INFO_OUTPUT_FORMAT_CHANGED width="+getRawDataWidth()+",height="+getRawDataHeight());
          MediaFormat outputFormat = getCodec().getOutputFormat();
          //Log.e(LogTab,"mine tye="+outputFormat.getString(outputFormat.KEY_MIME));
          final int bwidth = outputFormat.getInteger(MediaFormat.KEY_WIDTH);
          final int bheight = outputFormat.getInteger(MediaFormat.KEY_HEIGHT);
          EtxLogger.logE(TAG, getClass().getSimpleName(), "change bwidth="+bwidth+",bheight="+bheight+" change outputFormat=" +outputFormat);
          final int viewWidth = mTextureView.getWidth();
          final int viewHeight = mTextureView.getHeight();
          bitmapWIDTH = bwidth;
          bitmapHEIGHT = bheight;
          if ((viewWidth != 0 && viewHeight != 0 && bwidth != 0 && bheight != 0)) {
            surfaceWIDTH = viewWidth;
            surfaceHEIGHT = viewHeight;
            updateMatrix(1, 0, 0);
          }
          break;

        case MediaCodec.INFO_TRY_AGAIN_LATER:

          break;

        default:
          synchronized (surface){
            getCodec().releaseOutputBuffer(outputBufferIndex, true);
          }

          break;
      }

      if (isEos()) {
        EtxLogger.logE(TAG, getClass().getSimpleName(),  "MEET FLAG -> `END OF STREAM`");
        stopCodec();
        releaseCodec();
        mTextureView=null;
      }
    } catch (Exception ignored) {
    }
  }

  @Override public void startCodec() throws NullPointerException {
    if (isState(CodecState.PREPARING) || isState(CodecState.STOP)) {
      try {
        needFlush.set(false);
        setState(CodecState.PREPARED);
        super.startCodec();
        Log.e(TAG, "START CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "START CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "START CODEC -> failed, not configured");
      }
    } else {
      Log.e(TAG, "START CODEC -> failed, illegal state "+getState());
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
        Log.e(TAG, "STOP CODEC -> failed, no instance");
      } catch (IllegalStateException e) {
        e.printStackTrace();
        Log.e(TAG, "STOP CODEC -> failed, is in release state");
      }
    } else {
      Log.e(TAG, "STOP CODEC -> failed, illegal state");
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

        Log.e(TAG,"RELEASE CODEC -> succeeded");
      } catch (NullPointerException e) {
        setState(CodecState.UNINITIALIZED);
        Log.e(TAG, "RELEASE CODEC -> failed, no instance");
      }
      surface=null;

    } else {
      Log.e(TAG,"RELEASE CODEC -> failed, illegal state");
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


  private void initTransValue()
  {
    move = 0f;
    mscale=1;
    transX=0;
    transY=0;
  }

  public void setMaxScale (int scale) throws ArithmeticException
  {
    if(scale>=1&&scale<=6)
    {
      this.max_scale=scale;
      if(mscale>max_scale)
      {
        mscale=max_scale;
        updateMatrix(mscale,0,0);
      }

    }
    else throw new ArithmeticException("scale value must between 1~6");
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
        if (hasOneClick)
        {
          hasOneClick = false;

          doubleClick(event);
          return true;
        } else
        {
          mTextureView.postDelayed(doubleclick_run, 500);
          hasOneClick = true;
        }
      }
      // Log.e("123","isMove="+isMove+",move="+move+",mode="+mode+",isDrag="+isDrag);
      if ((isMove && premove == move) && mode == MODE_DOWN && mode != (MODE_UP + MODE_DOWN) )
      {


        if (mscale <= min_scale||mscale>max_scale)
          return true;

        if ( (mscale <= normal_scale_portrait))
        {
          int xx=transX+(int)((event.getX() - down_x));
          if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
          {
            transX=xx;
            updateMatrix(mscale, (int) (transX), 0);
          }

          // Log.e("123","ViewWidth="+surfaceWIDTH+",surfaceImageWIDTH="+surfaceImageWIDTH+",mscale="+mscale+",transX="+transX+",delta="+(mscale*surfaceImageWIDTH-surfaceWIDTH));
        }  else
        {
          int xx=transX+(int)((event.getX() - down_x));
          int yy=transY+(int)((event.getY() - down_y));
          if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
            transX=xx;
          if(yy<0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))
            transY=yy;
          updateMatrix(mscale, (int) (transX), (int) (transY));
          //Log.e("123", "ViewHeight=" + surfaceHEIGHT + ",surfaceImageWIDTH=" + surfaceImageHEIGHT + ",mscale=" + mscale + ",transY=" + transY + ",delta=" + (mscale*surfaceImageHEIGHT-surfaceHEIGHT));

        }

        down_x = event.getX();
        down_y = event.getY();
        isDrag = true;


      }else if ( mode == (MODE_DOWN + MODE_POINTER_DOWN))
      {
        if( event.getPointerCount()>=2)
        {
          float newDist = spacing(event);
          zoom(newDist / pre_Dist, down_x, down_y);
          pre_Dist = newDist;
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
        updateMatrix(mscale, 0, 0);
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
        updateMatrix(mscale,(int)(transX), (int)(transY));

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


      {
        if(normal_scale_portrait<max_scale)
        {
          if (mscale == min_scale)
          {
            initTransValue();
            mscale=normal_scale_portrait;
            updateMatrix(mscale,0,0);
          } else
          {
            initTransValue();
            mscale=min_scale;
            updateMatrix(min_scale, 0, 0);
          }
        }else
        {
          if (mscale == min_scale)
          {
            initTransValue();
            mscale=max_scale;
            updateMatrix(mscale,0,0);
          } else
          {
            initTransValue();
            mscale=min_scale;
            updateMatrix(min_scale, 0, 0);
          }
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


  private void updateMatrix(float scale,int Tx,int Ty)
  {

    if(!isState(CodecState.PREPARED)|| mTextureView ==null||bitmapWIDTH==0||bitmapHEIGHT==0||surfaceHEIGHT==0||surfaceWIDTH==0){
      return;
    }

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

    //mSurface.getSurfaceTexture().updateTexImage();
   // EtxLogger.logE(TAG, getClass().getSimpleName(), "updateMatrix bW=" + bitmapWIDTH + ",bH=" + bitmapHEIGHT + ",width=" + (scaleX*width)+ ",height=" + (scaleY*height));
  }

}