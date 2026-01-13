package com.acelink.etx.totalsecurity.aggregate.codec;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type;
import com.ns.greg.library.fancy_logger.FancyLogger;

import java.lang.ref.WeakReference;

class Sub4RTouch implements View.OnTouchListener, View.OnLayoutChangeListener {

    private Sub4RTouch(){}

    public static Mount_type mountType= Mount_type.WallOrDesk;

    private ImageView mView;

    int minY=60;//avoid high distort area

    private int bitmapWIDTH = 0;//test
    private int bitmapHEIGHT = 0;//test
    private int surfaceWIDTH = 0;
    private int surfaceHEIGHT = 0;
    private int surfaceImageWIDTH = 0;
    private int surfaceImageHEIGHT = 0;
    private int max_scale = 20;

    private float min_scale = 1f;
    private float normal_scale_portrait = 1f;
    private float premove = 0f;
    private float move = 0f;
    private float mscale = 1f;
    private int transX=0,transY=0;

    private int posInLive=0;

    private final float initWallScale=1.25f;
  //1.85f larger ,1.7 small er
    private final float initCeilingScale=1.78f;

    private final float initCeilingLandScale=2.4f;

    private float xCeilingScaleFactor=1.4f;//follow IOS X scale,pixel 7 as ip6

    private float xCeilingLandScaleFactor=1.25f;//follow IOS X scale,pixel 7 as ip6

    public Sub4RTouch(ImageView mView, int bitmapWIDTH, int bitmapHEIGHT, int posInLive){
        this.posInLive=posInLive;
        if(mView.getTag()!=null&&mView.getTag() instanceof Sub4RTouch)
        {
            mView.removeOnLayoutChangeListener((Sub4RTouch)mView.getTag());
        }
        mView.setTag(this);
        initTouch(mView,bitmapWIDTH,bitmapHEIGHT);
    }

    public void initTouch(ImageView mView,int bitmapWIDTH,int bitmapHEIGHT){
        WeakReference<ImageView> weakReference = new WeakReference<>(mView);
        this.mView = weakReference.get();
        this.bitmapWIDTH=bitmapWIDTH;
        this.bitmapHEIGHT=bitmapHEIGHT;
        minY=bitmapWIDTH<1800?200:60;


        mView.addOnLayoutChangeListener(this);
        if(mView.getWidth()!=0){

            surfaceWIDTH = mView.getWidth();
            surfaceHEIGHT = mView.getHeight();
            initTransValue();
            updateMatrix(0,0,true);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.getHeight() != 0 && v.getWidth() != 0) {
            if (surfaceWIDTH != v.getWidth()) {
                surfaceWIDTH = v.getWidth();
                surfaceHEIGHT = v.getHeight();
               // EtxLogger.log("Sub4RTouch", getClass().getSimpleName(),"Sub4RTouch onLayoutChange surfaceWIDTH=" + surfaceWIDTH + ",surfaceHEIGHT=" + surfaceHEIGHT);
                initTransValue();

                updateMatrix( 0,0, true);
            }
        }
    }

    public void initMatrix()
    {
        initTransValue();
        updateMatrix(0,0,true);
    }
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

        if(mView ==null)return false;
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

                //pre_Dist = spacing(event);

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
                if( move > 50)
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
           /* mView.removeCallbacks(doubleclick_run);
            //_controll.b_showbar = !_controll.b_showbar;
            if(m_myCallback!=null)m_myCallback.onLiveDoubleClick();
            if (hasOneClick)
            {
                hasOneClick = false;

                doubleClick(event);
                return true;
            } else
            {
                if(dewrap_mode!=Dewrap_mode.Dewrap_1R&&dewrap_mode!=Dewrap_mode.Dewrap_4R){
                    mView.postDelayed(doubleclick_run, 500);
                    hasOneClick = true;
                }

            }*/
        }
        // Log.e("123","isMove="+isMove+",move="+move+",mode="+mode+",isDrag="+isDrag);
        if ((isMove && premove == move) && mode == MODE_DOWN && mode != (MODE_UP + MODE_DOWN) )
        {


            if (mscale <= min_scale||mscale>max_scale){
                EtxLogger.logE("SubTouch", getClass().getSimpleName(), "SubTouch updateMatrix out of scale spec min_scale="+min_scale+",mscale="+mscale );
                return true;
            }
            /*EtxLogger.logE("Sub4RTouch", getClass().getSimpleName(), "Sub4RTouch mscale"+mscale +"normal_scale_portrait="+normal_scale_portrait);
            if ( (mscale <= normal_scale_portrait))
            {
                int xx=transX+(int)((event.getX() - down_x));
                if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
                {
                    transX=xx;
                    updateMatrix(mscale, (int) (transX), 0,false);
                }

                // Log.e("123","ViewWidth="+surfaceWIDTH+",surfaceImageWIDTH="+surfaceImageWIDTH+",mscale="+mscale+",transX="+transX+",delta="+(mscale*surfaceImageWIDTH-surfaceWIDTH));
            }  else
            {*/
                int xx=transX+(int)((event.getX() - down_x));
                int yy=transY+(int)((event.getY() - down_y));
                 if (mountType==Mount_type.WallOrDesk){
                   /*  int BW= DeFishWall.Companion.getBitmap1280WallWIDTH();
                     int BH= DeFishWall.Companion.getBitmap1280WallWIDTH();
                     if(bitmapWIDTH>1800){
                         BW= DeFishWall.Companion.getBitmap1944WallWIDTH();
                         BH= DeFishWall.Companion.getBitmap1944WallWIDTH();
                     }*/
                     if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
                         transX=xx;
                     if(yy<0.49*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*0.49*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))//-0.48 prevent black edge 0.29 prevent distort
                         transY=yy;
                     updateMatrix( (int) (transX), (int) (transY),false);
                 }
               else if (mountType==Mount_type.Direct)
                {
                    if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
                        transX=xx;
                    if(yy<0.29*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*0.48*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))//-0.48 prevent black edge 0.29 prevent distort
                        transY=yy;
                    updateMatrix( (int) (transX), (int) (transY),false);
                    // FancyLogger.e("123", "SubTouch ViewHeight=" + surfaceHEIGHT + ",surfaceImageHEIGHT=" + surfaceImageHEIGHT + ",mscale=" + mscale + ",transY=" + transY + ",deltaH=" + (0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))+",-deltaH="+(-1*0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)));

                }else {
                    if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
                        transX=xx;
                    if(yy<0.22*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*0.38*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))//-0.38 prevent black edge 0.29 prevent distort
                        transY=yy;
                    updateMatrix( (int) (transX), (int) (transY),false);
                   //  FancyLogger.e("123", "Sub4RTouch Ceiling ViewHeight=" + surfaceHEIGHT + ",surfaceImageHEIGHT=" + surfaceImageHEIGHT + ",mscale=" + mscale + ",transY=" + transY + ",deltaH=" + (0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))+",-deltaH="+(-1*0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)));

                }

           // }

            down_x = event.getX();
            down_y = event.getY();
            isDrag = true;


        }/*else if ( mode == (MODE_DOWN + MODE_POINTER_DOWN))
        {
          //  if(dewrap_mode!=Dewrap_mode.Dewrap_1R&&dewrap_mode!=Dewrap_mode.Dewrap_4R){
                if( event.getPointerCount()>=2)
                {
                    float newDist = spacing(event);
                    zoom(newDist / pre_Dist, down_x, down_y);
                    pre_Dist = newDist;
                }
           // }


        }*/
        return true;
    }


    private void initTransValue()
    {
        move = 0f;

        transX=0;
        transY=0;
        if(mountType==Mount_type.WallOrDesk){
            mscale=initWallScale;
        }else {
            if(surfaceWIDTH>surfaceHEIGHT*1.5){
                mscale=initCeilingLandScale;
            }else {
                mscale=initCeilingScale;
            }


        }

    }

    private void updateMatrix(int Tx,int Ty,boolean init)
    {

        if(//!isState(CodecState.PREPARED) ||
                mView ==null||bitmapWIDTH==0||bitmapHEIGHT==0||surfaceHEIGHT==0||surfaceWIDTH==0){
            return;
        }
        if(mountType==Mount_type.WallOrDesk){
            updateMatrix4RWall(initWallScale,Tx,Ty,init);
        }else {
            updateMatrix4RCeiling(Tx,Ty,init);
        }

        //mSurface.getSurfaceTexture().updateTexImage();

    }


    private void updateMatrix4RCeiling(int Tx, int Ty, boolean init){
//        int width = mView.getWidth();
//        int height = mView.getHeight();
        float ratioSurface = (float) surfaceWIDTH / surfaceHEIGHT;
        float ratioPreview = (float) bitmapWIDTH / bitmapHEIGHT;

        float scaleX;
        float scaleY;

        if (ratioSurface > ratioPreview)
        {
         //   scaleX = ((float) surfaceHEIGHT /bitmapHEIGHT)*((float) bitmapWIDTH /surfaceWIDTH);
            //scaleY = 1;
            scaleX=surfaceWIDTH/ (float) bitmapWIDTH;
            scaleY=surfaceWIDTH/ (float) bitmapWIDTH;
            normal_scale_portrait=1/scaleX;
        }
        else
        {
           // scaleX = 1;
          //  scaleY = (float) surfaceWIDTH / bitmapWIDTH*((float) bitmapHEIGHT /surfaceHEIGHT);
            scaleX=surfaceHEIGHT/ (float) bitmapHEIGHT;
            scaleY=surfaceHEIGHT/ (float) bitmapHEIGHT;
            normal_scale_portrait=1/scaleY;
        }
        scaleX=scaleY*xCeilingScaleFactor;
        if(surfaceWIDTH>surfaceHEIGHT*1.5) scaleX=scaleY*xCeilingLandScaleFactor;
      /*  Log.e("123","Ceiling mode "+",surfaceWIDTH="+surfaceWIDTH+",surfaceHEIGHT="+surfaceHEIGHT
                +",bitmapWIDTH="+bitmapWIDTH+",bitmapHEIGHT="+bitmapHEIGHT
                +",scaleX="+scaleX+",scaleY="+scaleY
                +",normal_scale_portrait ="+normal_scale_portrait);*/
        float scaledWidth;
        float scaledHeight;
        final Matrix matrix = new Matrix();
        surfaceImageWIDTH=(int)(bitmapWIDTH * scaleX);
        surfaceImageHEIGHT=(int)(bitmapHEIGHT * scaleY);
        float celingScale=(surfaceWIDTH>surfaceHEIGHT*1.5)?initCeilingLandScale : initCeilingScale;

        matrix.setScale(scaleX*celingScale, scaleY*celingScale);
         scaledWidth = surfaceImageWIDTH * celingScale;
         scaledHeight = (int)(surfaceImageHEIGHT *celingScale*0.85);//top distort


        float dx =(float)( (surfaceWIDTH - scaledWidth) / 2);
        float dy =(float)((surfaceHEIGHT - scaledHeight) / 2) ;
        //FancyLogger.e("123","sub tuoch scaleX= "+scaleX+",scaleY="+scaleY+",normal_scale_portrait="+normal_scale_portrait+",dx="+dx+",dy="+dy+",Tx="+Tx+",Ty="+Ty+",surfaceImageWIDTH="+surfaceImageWIDTH+",scaledWidth="+scaledWidth);
       if(init){

            if (mountType==Mount_type.Direct)
           {
               if(posInLive==0)Tx=(int)((surfaceImageWIDTH*celingScale)*0.31f);
               if(posInLive==1)Tx=(int)((surfaceImageWIDTH*celingScale)*(0.09f));
               if(posInLive==2)Tx=-(int)((surfaceImageWIDTH*celingScale)*(0.17f));
               if(posInLive==3)Tx=-(int)((surfaceImageWIDTH*celingScale)*(0.39f));
               transX=Tx;
               Ty=(int)((surfaceImageHEIGHT*celingScale)*(0.12f));//top distort
               transY=Ty;
           }else
           {
               if(posInLive==0){
                   Tx=(int)((surfaceImageWIDTH*celingScale)*0.40f);
                   Ty=(int)((surfaceImageHEIGHT*celingScale)*(0.05f));//top distort
                   if(surfaceWIDTH>surfaceHEIGHT*1.5)//landscape
                   {
                       Tx=(int)((surfaceImageWIDTH*celingScale)*0.375f);//can't greater 0.375,pixel 7 will black
                   }
               }
               if(posInLive==1){
                   Tx=(int)((surfaceImageWIDTH*celingScale)*(0.26f));
                   Ty=(int)((surfaceImageHEIGHT*celingScale)*(0.05f));//top distort
                   if(surfaceWIDTH>surfaceHEIGHT*1.5)//landscape
                   {
                       Tx=(int)((surfaceImageWIDTH*celingScale)*0.14f);
                   }
               }
               if(posInLive==2){
                   Tx=(int)((surfaceImageWIDTH*celingScale)*(-0.27f));
                   Ty=(int)((surfaceImageHEIGHT*celingScale)*(0.05f));//top distort
                   if(surfaceWIDTH>surfaceHEIGHT*1.5)
                   {
                       Tx=(int)((surfaceImageWIDTH*celingScale)*(-0.14f));
                       Ty=(int)((surfaceImageHEIGHT*celingScale)*(-0.02f));
                   }
               }
               if(posInLive==3){
                   Tx=(int)((surfaceImageWIDTH*celingScale)*(-0.405f));
                   Ty=(int)((surfaceImageHEIGHT*celingScale)*(0.05f));//top distort
                   if(surfaceWIDTH>surfaceHEIGHT*1.5){
                       Tx=(int)((surfaceImageWIDTH*celingScale)*-0.37f);
                       Ty=(int)((surfaceImageHEIGHT*celingScale)*(-0.02f));
                   }
               }
               transX=Tx;
               transY=Ty;

           }

       }
        matrix.postTranslate(dx + Tx, dy + Ty);
        //if(dx + Tx>)
        mView.post(new Runnable() {
            @Override
            public void run () {
                try
                {
                    synchronized (mView)
                    {
                      //  mView.setTransform(matrix);
                        mView.setImageMatrix(matrix);
                        mView.invalidate();
                    }
                }catch (Exception e){}
            }
        });
       // FancyLogger.e("SubTouch", "updateMatrix bW=" + bitmapWIDTH  + ",bH=" + bitmapHEIGHT +",scale="+scale+ ",surfaceImageWIDTH=" + surfaceImageWIDTH+ ",surfaceImageHEIGHT=" + surfaceImageHEIGHT+ ",viewwidth=" + surfaceWIDTH+ ",viewheight=" + surfaceHEIGHT);
    }

    private void updateMatrix4RWall(float scale,int Tx,int Ty,boolean init){
        int width = mView.getWidth();
        int height = mView.getHeight();
        float ratioSurface = (float) width / height;
        //float ratioPreview = (float) bitmapWIDTH / bitmapHEIGHT;
        float ratioPreview=1;
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
        scaleY=normal_scale_portrait;
        scaleX=normal_scale_portrait;
        final Matrix matrix = new Matrix();

        surfaceImageWIDTH=(int)(bitmapWIDTH * scaleX);
        surfaceImageHEIGHT=(int)(bitmapHEIGHT * scaleY);
        matrix.setScale(scaleX*scale, scaleY*scale);

        float scaledWidth = surfaceImageWIDTH * scale;
        float scaledHeight = (int)(surfaceImageHEIGHT *scale);
        float dx =(float)( (width-scaledWidth ) / 2);
        float dy =(float)((height-scaledHeight) / 2) ;
        FancyLogger.e("123","sub tuoch bitmapWIDTH="+bitmapWIDTH+" scaleX= "+scaleX+",scaleY="+scaleY+",normal_scale_portrait="+normal_scale_portrait+",dx="+dx+",dy="+dy+",Tx="+Tx+",Ty="+Ty+",surfaceImageWIDTH="+surfaceImageWIDTH+",scaledWidth="+scaledWidth);
        if(init){
            if(posInLive==0)Tx=(int)((surfaceImageWIDTH*scale)*0.25f);
            if(posInLive==1)Tx=-(int)((surfaceImageWIDTH*scale)*(0.2f));
            if(posInLive==2)Tx=(int)((surfaceImageWIDTH*scale)*(0.25f));
            if(posInLive==3)Tx=-(int)((surfaceImageWIDTH*scale)*(0.2f));
            transX=Tx;
            if(posInLive==0)Ty=(int)((surfaceImageHEIGHT*scale)*(0.1f));
            if(posInLive==1)Ty=(int)((surfaceImageHEIGHT*scale)*(0.1f));
            if(posInLive==2)Ty=-(int)((surfaceImageHEIGHT*scale)*(0.27f));
            if(posInLive==3)Ty=-(int)((surfaceImageHEIGHT*scale)*(0.27f));
            if(surfaceWIDTH>surfaceHEIGHT*1.6)//landscape
            {
                if(posInLive==0)Ty=(int)((surfaceImageHEIGHT*scale)*(0.0f));
                if(posInLive==1)Ty=(int)((surfaceImageHEIGHT*scale)*(0.0f));
                if(posInLive==2)Ty=-(int)((surfaceImageHEIGHT*scale)*(0.2f));
                if(posInLive==3)Ty=-(int)((surfaceImageHEIGHT*scale)*(0.2f));
            }
            transY=Ty;

        }
        matrix.postTranslate(dx + Tx, dy + Ty);
        //if(dx + Tx>)
        mView.post(new Runnable() {
            @Override
            public void run () {
                try
                {
                    synchronized (mView)
                    {
                        //  mView.setTransform(matrix);
                        mView.setImageMatrix(matrix);
                        mView.invalidate();
                    }
                }catch (Exception e){}
            }
        });
        // FancyLogger.e("SubTouch", "updateMatrix bW=" + bitmapWIDTH  + ",bH=" + bitmapHEIGHT +",scale="+scale+ ",surfaceImageWIDTH=" + surfaceImageWIDTH+ ",surfaceImageHEIGHT=" + surfaceImageHEIGHT+ ",viewwidth=" + surfaceWIDTH+ ",viewheight=" + surfaceHEIGHT);
    }

}