package com.acelink.etx.totalsecurity.aggregate.codec;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type;
import com.acelink.etx.totalsecurity.defish.DeFish1280;
import com.acelink.etx.totalsecurity.defish.DeFish1280Ceiling;
import com.acelink.etx.totalsecurity.defish.DeFish1944;
import com.acelink.etx.totalsecurity.defish.DeFish1944Ceiling;
import com.acelink.etx.totalsecurity.defish.DeFishWall;


import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

class Sub1OTo1R2Touch implements View.OnTouchListener {
    public static Mount_type mountType= Mount_type.WallOrDesk;
    private Sub1OTo1R2Touch(){}

    private ImageView mView;

    int minY=60;//top distort not view

    private int splitResWidth=1800;

    private int bitmapWIDTH = 0;//test
    private int bitmapHEIGHT = 0;//test

    private int surfaceWIDTH = 0;
    private int surfaceHEIGHT = 0;
    private int surfaceImageWIDTH = 0;
    private int surfaceImageHEIGHT = 0;
    private int max_scale = 5;

    private float min_scale = 1f;
    private float normal_scale_portrait = 1f;
    private float premove = 0f;
    private float move = 0f;
    private float mscale = 1f;
    private int transX=0,transY=0;

    private Bitmap map1O;

    private DeFish1280 deFish1280=new DeFish1280();

    private DeFish1944 deFish1944=new DeFish1944();

    private DeFish1280Ceiling deFish1280Ceiling=new DeFish1280Ceiling();

    private DeFish1944Ceiling deFish1944Ceiling=new DeFish1944Ceiling();

    private DeFishWall deFishWall=new DeFishWall();

    private AtomicBoolean isSetting=new AtomicBoolean(false);
    public void setMap1O(Bitmap map1O) {
        if(this.map1O==null||Math.abs(this.map1O.getWidth()-map1O.getWidth())>20){
            this.map1O = map1O;
            initTransValue();
        }else {
            this.map1O = map1O;
        }


    }

   /* public void forceSetImageBitmap(){
        isSetting.set(false);
        setImageBitmap();
    }*/

    public void setImageBitmap(){
        try {
            if(isSetting.compareAndSet(false,true))
            {
                Bitmap map1R=null;
              //  Log.e("123","Sub1OTo1R2Touch setImageBitmap="+",transX="+transX+",transY"+transY);
                if(Math.abs(map1O.getWidth())-1280<=20){
                    if(mountType== Mount_type.Ceiling){
                        if(deFish1280Ceiling.getMatrix()==null)deFish1280Ceiling.deFish1280(map1O);
                        //Log.e("123","deFish1280 is matrix"+(deFish1280.getMatrix()==null));
                        map1R=deFish1280Ceiling.deFish12801R(map1O,transX,transY);

                    }else if(mountType== Mount_type.WallOrDesk){

                        map1R=deFishWall.deFish12801R(map1O,transX,transY);
                    }else {
                        if(deFish1280.getMatrix()==null)deFish1280.deFish1280(map1O);
                        //Log.e("123","deFish1280 is matrix"+(deFish1280.getMatrix()==null));
                        map1R=deFish1280.deFish12801R(map1O,transX,transY);
                    }

                }else if(Math.abs(map1O.getWidth())-1944<=20)
                {
                    if(mountType== Mount_type.Ceiling){
                        if(deFish1944Ceiling.getMatrix()==null)deFish1944Ceiling.deFish1944(map1O);
                        //Log.e("123","deFish1944 is matrix"+(deFish1944.getMatrix()==null));
                        map1R=deFish1944Ceiling.deFish19441R(map1O,transX,transY);
                    }else if(mountType== Mount_type.WallOrDesk){
                        map1R=deFishWall.deFish19441R(map1O,transX,transY);
                    }else {
                        if(deFish1944.getMatrix()==null)deFish1944.deFish1944(map1O);
                        //Log.e("123","deFish1944 is matrix"+(deFish1944.getMatrix()==null));
                        map1R=deFish1944.deFish19441R(map1O,transX,transY);
                    }


                }
                //Log.e("123","map1R is null"+(map1R==null));
                if(map1R!=null){
                    final Bitmap m=map1R;
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            mView.setImageBitmap(m);
                        }
                    });

                }
                isSetting.set(false);
            }
        }catch (Exception e){
            e.printStackTrace();
            isSetting.set(false);
        }


    }

    public Sub1OTo1R2Touch(ImageView mView, int bitmapWIDTH, int bitmapHEIGHT){

        initTouch(mView,bitmapWIDTH,bitmapHEIGHT);
    }

    public void initTouch(ImageView mView,int bitmapWIDTH,int bitmapHEIGHT){
        WeakReference<ImageView> weakReference = new WeakReference<>(mView);
        this.mView = weakReference.get();
        this.bitmapWIDTH=bitmapWIDTH;
        this.bitmapHEIGHT=bitmapHEIGHT;
        minY=bitmapWIDTH<splitResWidth?60:80;
        if(mView.getWidth()==0){

            mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (v.getHeight() != 0 && v.getWidth() != 0) {
                        if (surfaceWIDTH != v.getWidth()) {
                            surfaceWIDTH = v.getWidth();
                            surfaceHEIGHT = v.getHeight();
                            //EtxLogger.log(TAG, getClass().getSimpleName(),"onLayoutChange surfaceWIDTH=" + surfaceWIDTH + ",surfaceHEIGHT=" + surfaceHEIGHT);
                            initMatrix();
                        }
                    }
                }
            });
        }else {
            surfaceWIDTH = mView.getWidth();
            surfaceHEIGHT = mView.getHeight();
            initMatrix();
        }
    }

    public void initMatrix()
    {
        initTransValue();
        updateMatrix();
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

              //  pre_Dist = spacing(event);

                break;

            case MotionEvent.ACTION_UP:
                if(!isDrag)
                    mode += MODE_UP;

                up_x = event.getX();

                up_y = event.getY();

                move = ((Math.abs(down_x - up_x)) + (Math.abs(down_y - up_y)));
                if( move < 20 )
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
                if( move > 20)
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


      /*  if (!isMove &&  (mode == (MODE_UP + MODE_DOWN))) {
            mView.removeCallbacks(doubleclick_run);
            //_controll.b_showbar = !_controll.b_showbar;
           // if(mView!=null)mView.onLiveDoubleClick();
            if (hasOneClick)
            {
                hasOneClick = false;

                doubleClick(event);
                return true;
            } else
            {

                    mView.postDelayed(doubleclick_run, 200);
                    hasOneClick = true;


            }
        }*/
         //Log.e("123","Sub1OTo1R2Touch isMove="+isMove+",mscale="+mscale+",normal_scale_portrait="+normal_scale_portrait+",move="+move+",premove"+premove+",mode="+mode+",isDrag="+isDrag);
        if (map1O!=null&&(isMove && premove == move) && mode == MODE_DOWN && mode != (MODE_UP + MODE_DOWN) )
        {


       //     if (mscale <= min_scale||mscale>max_scale)
         //       return true;
            int xx=transX-(int)((event.getX() - down_x)*0.5);
            int yy=transY-(int)((event.getY() - down_y)*0.5);

            int width1R=bitmapWIDTH<splitResWidth?(DeFish1280.Companion.getWidth1R1280())+5:(DeFish1944.Companion.getWidth1R1944()+8);

        //    Log.e("123","Sub1OTo1R2Touch width1R="+width1R+", xx="+xx+",transX="+transX+",dev="+(bitmapWIDTH-xx));
            //distort serious
            boolean change=false;
            //x axis
            if(mountType==Mount_type.WallOrDesk){
                if (map1O!=null)
                {
                    int w=map1O.getWidth();
                    int widthWall1R=w<splitResWidth?(DeFishWall.Companion.getWidthWall1R1280()):(DeFishWall.Companion.getWidthWall1R1944());
                    int wallWidth=w<splitResWidth?(DeFishWall.Companion.getBitmap1280WallWIDTH()):DeFishWall.Companion.getBitmap1944WallWIDTH();
                    if(xx>=0&&wallWidth-xx>widthWall1R){
                        change=true;
                        transX=xx;
                    }
                }
            }else {
                if(xx>=0&&bitmapWIDTH-xx>width1R){
                    change=true;
                    transX=xx;
                }
            }
            //y axis
            if(mountType==Mount_type.WallOrDesk){
                if (map1O!=null)
                {
                    int w=map1O.getWidth();
                    int widthWall1R=w<splitResWidth?(DeFishWall.Companion.getWidthWall1R1280()):(DeFishWall.Companion.getWidthWall1R1944());
                    int wallWidth=w<splitResWidth?(DeFishWall.Companion.getBitmap1280WallWIDTH()):DeFishWall.Companion.getBitmap1944WallWIDTH();
                    if(yy>=0&&wallWidth-yy>widthWall1R)
                    {
                        change=true;
                        transY=yy;
                    }
                }
            }else  if(mountType==Mount_type.Direct)
            {
                if(yy>=minY&&bitmapHEIGHT-yy>width1R)//slice top
                {
                    change=true;
                    transY=yy;
                }
            }else if(mountType==Mount_type.Ceiling)//slice bottom
            {
                if(yy>=30&&bitmapHEIGHT-yy-minY>width1R){
                    change=true;
                    transY=yy;
                }
            }

               if(change){
                   updateMatrix();
               }


            down_x = event.getX();
            down_y = event.getY();
            isDrag = true;


        }/*else if ( mode == (MODE_DOWN + MODE_POINTER_DOWN))
        {

                if( event.getPointerCount()>=2)
                {
                    float newDist = spacing(event);
                    zoom(newDist / pre_Dist, down_x, down_y);
                    pre_Dist = newDist;
                }

        }*/
        return true;
    }

    public void initTransValue()
    {
        move = 0f;
        if(mountType==Mount_type.WallOrDesk){
                if(map1O!=null){
                    int w=map1O.getWidth();
                    if(w>1500){

                        double differ=(DeFishWall.Companion.getBitmap1944WallWIDTH()-DeFishWall.Companion.getWidthWall1R1944())*0.5;
                        transX=(int)(differ*0.6);
                        transY=(int)(differ*0.6);
                        Log.e("123","Dewrap 1944 wall initTransValue differ="+differ);
                    }else {
                        double differ=(DeFishWall.Companion.getBitmap1280WallWIDTH()-DeFishWall.Companion.getWidthWall1R1280())*0.5;
                        transX=(int)(differ*0.6);
                        transY=(int)(differ*0.6);
                        Log.e("123","Dewrap 1280 wall initTransValue differ="+differ);
                    }
                }

        }else {
            if(bitmapWIDTH>splitResWidth)//resolution high
            {
                //float scaleX=surfaceWIDTH/DeFish1944.Companion.getWidth1R();
                // float scaleY=surfaceHEIGHT/DeFish1944.Companion.getWidth1R();

                if(mountType==Mount_type.Direct){
                    transX=(int)((bitmapWIDTH-DeFish1944.Companion.getWidth1R1944())*0.5);
                    transY=(int)((bitmapHEIGHT-minY-DeFish1944.Companion.getWidth1R1944())*0.4)+minY;//minY->top distort
                }else {
                    transX=(int)((bitmapWIDTH-DeFish1944.Companion.getWidth1R1944())*0.42);
                    if(surfaceWIDTH>surfaceHEIGHT*1.5){
                        transY=(int)((bitmapHEIGHT-minY-DeFish1944.Companion.getWidth1R1944())*0.8);
                    }else {
                        transY=(int)((bitmapHEIGHT-minY-DeFish1944.Companion.getWidth1R1944())*0.6);
                    }

                }

            }else {
                //  float scaleX=surfaceWIDTH/DeFish1280.Companion.getWidth1R1280();
                //  float scaleY=surfaceHEIGHT/DeFish1280.Companion.getWidth1R1280();

                if(mountType==Mount_type.Direct){
                    transX=(int)((bitmapWIDTH-DeFish1280.Companion.getWidth1R1280())*0.5);
                    transY=(int)((bitmapHEIGHT-minY-DeFish1280.Companion.getWidth1R1280())*0.4)+minY;
                }else {
                    transX=(int)((bitmapWIDTH-DeFish1280.Companion.getWidth1R1280())*0.85);
                    transY=(int)((bitmapHEIGHT-minY-DeFish1280.Companion.getWidth1R1280())*0.18);
                }

            }
        }


       // if(dewrap_mode==Dewrap_mode.Dewrap_1R||dewrap_mode==Dewrap_mode.Dewrap_4R){
            mscale=1;
     /*   }else {
            mscale=1;
        }*/
    }

    private void updateMatrix()
    {
       new Thread(new Runnable() {
           @Override
           public void run() {
               if(map1O!=null)setImageBitmap();
           }
       }).start();



    }




}