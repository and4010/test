package com.acelink.etx.totalsecurity.defish

import android.graphics.Bitmap
import android.util.Log

class DeFishWall {
    //var pixelDataOTemp2 =IntArray(1947*620,{i->0})// Array<Int>(repeating: 0, count: 1947*620)
   // var undistPixel =IntArray(1947*620,{i->0})
    companion object{

        public val bitmap1280WallWIDTH: Int = 650
        public val bitmap1944WallWIDTH = 950// arrowsF-01K will only 1920*1088
        public val widthWall1R1944=680
        public val widthWall1R1280=570
    }


    public fun deFish12801P( bitmap:Bitmap):Bitmap{

        val t0=System.currentTimeMillis()
        var  i = 0

        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)

        var height = bitmap.height
        var width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cx = ( height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var Wd=bitmap1280WallWIDTH
        var Hd=bitmap1280WallWIDTH
        var start=-(bitmap1280WallWIDTH*0.5).toInt()

        // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var xS = 0.0
        var yS = 0.0
        var y = 0
        val result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
        val outPixels = IntArray(Wd * Hd)
      //  Log.e("123"," defish wall 1280 Wd=${Wd} R=${start} cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd - 1 ){

                val DX=x+start
                val DY=y+start
                xS = (Cx) +DX
                yS = (Cx) +DY
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                outPixels[y*Wd+x]=colour

                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)
        // Log.e("123","i="+i)
        Log.e("123"," defish Wall 1P time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }

    public fun deFish12801R( bitmap:Bitmap,xx:Int,yy:Int):Bitmap{

        val t0=System.currentTimeMillis()
        var  i = 0

        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)

        var height = bitmap.height
        var width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cx = ( height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var Wd=widthWall1R1280
        var Hd=widthWall1R1280
        var start=-(bitmap1280WallWIDTH*0.5).toInt()

        // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var xS = 0.0
        var yS = 0.0
        var y = 0
        val result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
        val outPixels = IntArray(Wd * Hd)
      //  Log.e("123"," defish wall 1280 Wd=${Wd} R=${start} cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd- 1 ){

                val DX=x+xx+start
                val DY=y+yy+start
                xS = (Cx) +DX
                yS = (Cy) +DY
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                outPixels[y*Wd+x]=colour

                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)
        // Log.e("123","i="+i)
        //Log.e("123"," defish time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }

    public fun deFish19441P( bitmap:Bitmap):Bitmap{

        val t0=System.currentTimeMillis()
        var  i = 0

        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)

        var height = bitmap.height
        var width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cx = ( height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var Wd=bitmap1944WallWIDTH
        var Hd=bitmap1944WallWIDTH
        var start=-(bitmap1944WallWIDTH*0.5).toInt()


       // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var xS = 0.0
        var yS = 0.0
        var y = 0
        val result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
        val outPixels = IntArray(Wd * Hd)
       // Log.e("123"," defish wall 1944 Wd=${Wd} R=${start} cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd - 1 ){


                    val DX=x+start
                    val DY=y+start
                    xS = (Cx) +DX
                    yS = (Cy) +DY
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                    val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                    outPixels[y*Wd+x]=colour

                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)
        // Log.e("123","i="+i)
       // Log.e("123"," defish Wall 1944 1P time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }


    public fun deFish19441R( bitmap:Bitmap,xx:Int,yy:Int):Bitmap{

        val t0=System.currentTimeMillis()
        var  i = 0

        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)

        var height = bitmap.height
        var width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cx = ( height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var Wd=widthWall1R1944
        var Hd=widthWall1R1944
        var start=-(bitmap1944WallWIDTH*0.5).toInt()

        // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var xS = 0.0
        var yS = 0.0
        var y = 0
        val result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
        val outPixels = IntArray(Wd * Hd)
      //  Log.e("123"," defish wall 1280 Wd=${Wd} R=${start} cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd- 1 ){

                val DX=x+xx+start
                val DY=y+yy+start
                xS = (Cx) +DX
                yS = (Cx) +DY
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                outPixels[y*Wd+x]=colour

                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)
        // Log.e("123","i="+i)
        //Log.e("123"," defish time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }





}