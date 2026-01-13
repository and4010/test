package com.acelink.etx.totalsecurity.defish

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
//Deprecated
class DeFish1944Wall {
    //var pixelDataOTemp2 =IntArray(1947*620,{i->0})// Array<Int>(repeating: 0, count: 1947*620)
   // var undistPixel =IntArray(1947*620,{i->0})

    companion object{
        val width1R1944=320
    }

     var matrix:Array<IntArray>?=null
    private var matrixY:Array<IntArray>?=null
    var height = 1944//1280
    var width =  1944//1280
    var offset= 0//1280


    public fun deFish1944( bitmap:Bitmap):Bitmap{


        val t0=System.currentTimeMillis()

        var Wd = 1655//1947
        var Hd = 1655//620
        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)

        height = bitmap.height
        width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cb = 265//20
   //     var offset=1280
        var Cx = (height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var R = (Cx-Cb).toInt()
        var PI = 3.1415926535897932384626433832795
        //Wd = (Math.abs(2.0 * ((R / 2)).toDouble() * PI)).toInt() //1947
       // Hd  = (Math.abs(R)) .toInt()//620
        Wd=Math.abs(2.0 * R*Math.sin(Math.toRadians(45.0))).toInt()
        Hd=Wd
       // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var pixelDataO = 0
        var xS = 0.0
        var yS = 0.0
        var y = 0
        val result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
        val outPixels = IntArray(Wd * Hd)
        Log.e("123"," defish 1944 cretae time=${(System.currentTimeMillis()-t0)}  " )
        if(matrix==null){
            matrix= Array(Wd) {IntArray(Hd) {-1} }
            matrixY= Array(Wd) {IntArray(Hd) {-1} }
        }
        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd - 1 ){
                if(matrix!![x][y]==-1){

                   // var positonR = (y)*(R / Hd)
                  //  var theta = (((x - offset).toDouble() / (Wd).toDouble()) * 2.0 * PI)

                  //  xS = (Cx) + positonR * Math.sin((theta))
                   // yS = (Cy) + positonR * Math.cos((theta))
                    val DX=x+(R*Math.cos(Math.toRadians(135.0))).toInt()
                    val DY=y+(R*Math.cos(Math.toRadians(135.0))).toInt()
                    xS = (Cx) +DX
                    yS = (Cx) +DY
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                    val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                    outPixels[y*Wd+x]=colour
                    matrix!![x][y]=xS.toInt()
                    matrixY!![x][y]=yS.toInt()

                }else{
                    val  xx= matrix!![x][y]
                    val  yy= matrixY!![x][y]
                    val colour = pixels[(yy.toInt()*BitmapWidth+xx.toInt())]
                    outPixels[y*Wd+x]=colour
                }



                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)

       // Log.e("123"," defish time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }





    public fun deFish19441R( bitmap:Bitmap,dx:Int,dy:Int):Bitmap?{
        if(matrix==null){
            Log.e("123"," defish 1944 matrix not init " )
            return null;
        }
        val t0=System.currentTimeMillis()
        var  i = 0
        var Wd = 1947
        var Hd = 620

        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)
//         val colour = pixels[640*bitmap.width+640]
//        val red = Color.red(colour)
//        val green = Color.green(colour)
//        val blue = Color.blue(colour)
        //  Log.e("123","i= 640*640=${red}  gg=${green} bb=${blue} " )
        var height =  bitmap.width
        var width = bitmap.height

        var Cb = 265
    //    var offset=1280
        var Cx = (height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var R = (Cb - Cx).toInt()
        var PI = 3.1415926535897932384626433832795
        Wd = (Math.abs(2.0 * ((R / 2)).toDouble() * PI)).toInt() //1947
        Hd  = (Math.abs(R)) .toInt()//620

        var pixelDataO = 0
       // var xS = 0.0
    //    var yS = 0.0
        var y = dy
        val result = Bitmap.createBitmap(width1R1944, width1R1944,Bitmap.Config.RGB_565)
        val outPixels = IntArray(width1R1944 * width1R1944)
        Log.e("123"," deFish19441R cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= dy+width1R1944-1) {
            var x = dx
            while (x <=dx+width1R1944-1 ){

                    val  xx= matrix!![x][y]
                    val  yy= matrixY!![x][y]
                    val colour = pixels[(yy.toInt()*BitmapWidth+xx.toInt())]
                    var outIndex=(y-dy)*width1R1944+(x-dx)
                    if (outIndex<outPixels.size) outPixels[outIndex]=colour
                    else Log.e("123","deFish19441R outIndex="+outIndex);


                i += 1
                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, width1R1944, 0, 0,width1R1944, width1R1944)
        // Log.e("123","i="+i)
        Log.e("123"," defish 1R time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }


}