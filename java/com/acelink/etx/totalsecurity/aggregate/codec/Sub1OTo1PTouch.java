package com.acelink.etx.totalsecurity.aggregate.codec;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.acelink.etx.EtxLogger;
import com.ns.greg.library.fancy_logger.FancyLogger;

import java.lang.ref.WeakReference;

class Sub1OTo1PTouch implements View.OnTouchListener {

    private Sub1OTo1PTouch(){}

    private ImageView mView;

    private int bitmapWIDTH = 0;//test
    private int bitmapHEIGHT = 0;//test
    private int surfaceWIDTH = 0;
    private int surfaceHEIGHT = 0;
    private int surfaceImageWIDTH = 0;
    private int surfaceImageHEIGHT = 0;
    private int max_scale = 4;

    private float min_scale = 1f;
    private float normal_scale_portrait = 1f;
    private float premove = 0f;
    private float move = 0f;
    private float mscale = 1f;
    private int transX=0,transY=0;

    public Sub1OTo1PTouch(ImageView mView, int bitmapWIDTH, int bitmapHEIGHT){

        initTouch(mView,bitmapWIDTH,bitmapHEIGHT);
    }

    public void initTouch(ImageView mView,int bitmapWIDTH,int bitmapHEIGHT){
        WeakReference<ImageView> weakReference = new WeakReference<>(mView);
        this.mView = weakReference.get();
        this.bitmapWIDTH=bitmapWIDTH;
        this.bitmapHEIGHT=bitmapHEIGHT;

        if(mView.getWidth()==0){

            mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (v.getHeight() != 0 && v.getWidth() != 0) {
                        if (surfaceWIDTH != v.getWidth()) {
                            surfaceWIDTH = v.getWidth();
                            surfaceHEIGHT = v.getHeight();
                            float ratioSurface = (float) surfaceWIDTH / surfaceHEIGHT;
                            EtxLogger.log("Sub1OTo1PTouch", getClass().getSimpleName(),"onLayoutChange surfaceWIDTH=" + surfaceWIDTH + ",surfaceHEIGHT=" + surfaceHEIGHT+",ratioSurface="+ratioSurface+",bitmapWIDTH="+bitmapWIDTH+",bitmapHEIGHT="+bitmapHEIGHT);
                            initMatrix();
                        }
                    }
                }
            });
        }else {
            surfaceWIDTH = mView.getWidth();
            surfaceHEIGHT = mView.getHeight();
            float ratioSurface = (float) surfaceWIDTH / surfaceHEIGHT;
            EtxLogger.log("Sub1OTo1PTouch", getClass().getSimpleName(),"initLayout surfaceWIDTH 2=" + surfaceWIDTH + ",surfaceHEIGHT=" + surfaceHEIGHT+",ratioSurface="+ratioSurface+",bitmapWIDTH="+bitmapWIDTH+",bitmapHEIGHT="+bitmapHEIGHT);
            initMatrix();
        }
    }

    public void initMatrix()
    {
        initTransValue();
        updateMatrix(1,0,0,true);
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
            mView.removeCallbacks(doubleclick_run);
            //_controll.b_showbar = !_controll.b_showbar;
           // if(mView!=null)mView.onLiveDoubleClick();
            if (hasOneClick)
            {
                hasOneClick = false;
                 if(bitmapHEIGHT!=bitmapWIDTH)   //wall mount no doubleClick
                    doubleClick(event);
                return true;
            } else
            {

                    mView.postDelayed(doubleclick_run, 200);
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
                    updateMatrix(mscale, (int) (transX), 0,false);
                }

                EtxLogger.log("Sub1OTo1PTouch","Sub1OTo1PTouch 1 ViewWidth="+surfaceWIDTH+",surfaceImageWIDTH="+surfaceImageWIDTH+",mscale="+mscale+",transX="+transX+",delta="+(mscale*surfaceImageWIDTH-surfaceWIDTH));
            }  else
            {
                int xx=transX+(int)((event.getX() - down_x));
                int yy=transY+(int)((event.getY() - down_y));
                if(xx<(mscale*surfaceImageWIDTH-surfaceWIDTH)*0.5&&xx>-1*0.5*(mscale*surfaceImageWIDTH-surfaceWIDTH))
                    transX=xx;
                if(yy<0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT)&&yy>-1*0.5*(mscale*surfaceImageHEIGHT-surfaceHEIGHT))
                    transY=yy;
                updateMatrix(mscale, (int) (transX), (int) (transY),false);
                EtxLogger.log("Sub1OTo1PTouch", "Sub1OTo1PTouch 2 ViewHeight=" + surfaceHEIGHT + ",surfaceImageWIDTH=" + surfaceImageHEIGHT + ",mscale=" + mscale + ",transY=" + transY + ",delta=" + (mscale*surfaceImageHEIGHT-surfaceHEIGHT));

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
        }else if (mscale* f <= original_s_w|| mscale * f <= original_s_w)
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
       Log.e("123","1P double click");
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
    private void initTransValue()
    {
        move = 0f;

        transX=0;
        transY=0;
       // if(dewrap_mode==Dewrap_mode.Dewrap_1R||dewrap_mode==Dewrap_mode.Dewrap_4R){
            mscale=1;
     /*   }else {
            mscale=1;
        }*/
    }

    private void updateMatrix(float scale,int Tx,int Ty,boolean isInit)
    {

        if(//!isState(CodecState.PREPARED) ||
                mView ==null||bitmapWIDTH==0||bitmapHEIGHT==0||surfaceHEIGHT==0||surfaceWIDTH==0){
            return;
        }
        int width = mView.getWidth();
        int height = mView.getHeight();
        float ratioSurface = (float) width / height;
        float ratioPreview = (float) bitmapWIDTH / bitmapHEIGHT;

        float scaleX=0f;//normalize scale
        float scaleY=0f;//normalize scale

        if(bitmapWIDTH==bitmapHEIGHT)//wall mount 1P
        {
            float scaleM;//scale low
            float scaleH;//scale high
            if (ratioSurface > ratioPreview)//ratioPreview=1
            {
                scaleM= (float)(height/(float)bitmapHEIGHT);
                scaleH  = (float)(width/(float)bitmapWIDTH);
               // normal_scale_portrait=1/scaleM;
                normal_scale_portrait=1;
                scaleX=scaleH;
                scaleY=scaleH;
                if(isInit){
                   // scale=scaleH;
                    max_scale=5;

                }
            }
            else
            {
                 scaleM=(float)(width/(float)bitmapWIDTH);
                scaleH=(float)(height/(float)bitmapHEIGHT);
                //normal_scale_portrait=1/scaleM;
                normal_scale_portrait=1;
                scaleX=scaleH;
                scaleY=scaleH;
                if(isInit){
                    max_scale=4;
                }
            }

            if(scale<=1)scale=1+0.01f;
        }else {//Celing mount
            if (ratioSurface > ratioPreview)//this
            {
                scaleX = (float)(height/(float)bitmapHEIGHT);
                scaleY = (float)(height/(float)bitmapHEIGHT);
                normal_scale_portrait=1/scaleX;
            }
            else
            {

                scaleX=(float)(width/(float)bitmapWIDTH);
                scaleY=(float)(width/(float)bitmapWIDTH);
                normal_scale_portrait=1/scaleY;
            }
        }

    /*  if(surfaceWIDTH<surfaceHEIGHT*1.6)//portrait,wall mount need larger to fit landscape
        {
            float min=2f;
           // if(mscale<min) mscale=min;
            if (isInit)scale=mscale;
        }*/
        final Matrix matrix = new Matrix();
        surfaceImageWIDTH=(int)(bitmapWIDTH*scaleX );
        surfaceImageHEIGHT=(int)(bitmapHEIGHT *scaleY);
        matrix.setScale(scaleX*scale, scaleY*scale);
        float scaledWidth = surfaceImageWIDTH * scale;
        float scaledHeight = surfaceImageHEIGHT *scale;
        float dx =(float)( (width - scaledWidth) / 2);
        float dy =(float)((height - scaledHeight) / 2) ;
        matrix.postTranslate(dx + Tx, dy + Ty);
        //if(dx + Tx>)
        mView.post(new Runnable() {
            @Override
            public void run () {
                try
                {
                    synchronized (mView)
                    {
                        mView.setImageMatrix(matrix);
                        mView.invalidate();
                    }
                }catch (Exception e){}
            }
        });
        //  EtxLogger.logE("Sub1OTo1PTouch", getClass().getSimpleName(), "updateMatrix bW=" + bitmapWIDTH + ",bH=" + bitmapHEIGHT + ",width=" + (scaleX*width)+ ",height=" + (scaleY*height));
        EtxLogger.log("Sub1OTo1PTouch", "Sub1OTo1PTouch updateMatrix scale="+scale+ ",width=" + (width)+ ",height=" + (height)+ ",scaledWidth=" +scaledWidth+" scaledHeight=" + scaledHeight + ",scaleX=" + scaleX+",Tx="+Tx );



        //mSurface.getSurfaceTexture().updateTexImage();

    }




}