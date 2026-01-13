package com.acelink.etx.totalsecurity.defish

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

class DeFish1944Ceiling {
    //var pixelDataOTemp2 =IntArray(1947*620,{i->0})// Array<Int>(repeating: 0, count: 1947*620)
   // var undistPixel =IntArray(1947*620,{i->0})



     var matrix:Array<IntArray>?=null
     var matrixY:Array<IntArray>?=null
    var height = 1944//1280
    var width =  1944//1280


    var temp1PCeilingMap:Bitmap?=null
    var tempOut:IntArray?=null
     inline fun deFish1944( bitmap:Bitmap):Bitmap
    {

        // val colour = bitmap.getPixel(640, 640)
//        val red = Color.red(colour)
//        val green = Color.green(colour)
//        val blue = Color.blue(colour)
//        Log.e("123","i= 640*640=${red}  gg=${green} bb=${blue} " )
        val t0=System.currentTimeMillis()
        var  i = 0
        var Wd =2957//A32 not enough,so to 1947
        var Hd =942
       // var Wd = 1947
     //   var Hd = 620//942
        // Create an Image anyhow you want
        val BitmapWidth=bitmap.width
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width,0, 0, bitmap.width, bitmap.height)
//         val colour = pixels[640*bitmap.width+640]
//        val red = Color.red(colour)
//        val green = Color.green(colour)
//        val blue = Color.blue(colour)
        //  Log.e("123","i= 640*640=${red}  gg=${green} bb=${blue} " )
      //  var height = 1280
      //  var width = 1280
        height = bitmap.height
        width = bitmap.width
        //offset=(bitmap.width*0.5).toInt()
        var Cb = (Wd*0.09f).toInt()//265
        var offset=0//(bitmap.width*0.5).toInt()
        var Cx = (height / 2f).toDouble()
        var Cy = (width / 2f).toDouble()

        var R = (Cb - Cx).toInt()
        var PI = 3.1415926535897932384626433832795
        Wd = (Math.abs(2.0 * ((R / 2)).toDouble() * PI)).toInt()
        Hd  = (Math.abs(R)) .toInt()
       // Log.e("123"," defish Wd =${Wd}  Hd =${Hd} " )
        var pixelDataO = 0
        var xS = 0.0
        var yS = 0.0
        var y = 0
        var result : Bitmap?=null
        var outPixels :IntArray?=null
        if (temp1PCeilingMap!=null&&temp1PCeilingMap!!.width==Wd&&temp1PCeilingMap!!.height==Hd)
        {
            result=temp1PCeilingMap;
            outPixels=tempOut!!;
        }else {
            result = Bitmap.createBitmap(Wd, Hd,Bitmap.Config.RGB_565)
            outPixels = IntArray(Wd * Hd)
            temp1PCeilingMap=result
            tempOut=outPixels!!
        }

        //Log.e("123"," DeFish1944Ceiling cretae time=${(System.currentTimeMillis()-t0)}  " )
        if(matrix==null){
            matrix= Array(Wd) {IntArray(Hd) {-1} }
            matrixY= Array(Wd) {IntArray(Hd) {-1} }
        }
      //  Log.e("123"," defish 1944 ceiling 1P time1=${(System.currentTimeMillis()-t0)}  " )
        val mm=matrix!!//local var speed up
        val mY=matrixY!!
        while( y <= Hd - 1) {
            var x = 0
            while (x <= Wd - 1 ){
                var xr=Wd - 1-x
                var yr=Hd-1-y
                if(mm[xr][yr]==-1){

                    var positonR = (y)*(R / Hd)
                    var theta = (((x - offset).toDouble() / (Wd).toDouble()) * 2.0 * PI)
                    xS = (Cx) + positonR * Math.sin((theta))
                    yS = (Cy) + positonR * Math.cos((theta))
//                val colour = bitmap.getPixel(xS.toInt(), yS.toInt())
                    val colour = pixels[(yS.toInt()*BitmapWidth+xS.toInt())]
                    outPixels[yr*Wd+xr]=colour
                    mm[xr][yr]=xS.toInt()
                    mY[xr][yr]=yS.toInt()

                }else{
                    val  xx= mm[x][y]
                    val  yy= mY[x][y]
                    val colour = pixels[(yy.toInt()*BitmapWidth+xx.toInt())]
                    outPixels[y*Wd+x]=colour
                }



                //  result.setPixel(x,y,Color.rgb(red,green,blue))

                //     pixelDataO = ((width * (yS) .toInt()) + (xS) .toInt()) * numberOfComponents
                //   pixelDataOTemp2[i]=pixelDataO

                i += 1
                x += 1
            }
            y += 1
        }
    //    Log.e("123"," defish 1944 ceiling 1P time2=${(System.currentTimeMillis()-t0)}  " )
        result!!.setPixels(outPixels, 0, Wd, 0, 0,Wd, Hd)
        // Log.e("123","i="+i)
        val map=result.copy(result.config!!,result.isMutable)
      //  Log.e("123"," defish 1944 ceiling 1P time3=${(System.currentTimeMillis()-t0)}  " )
        return map
    }





    public fun deFish19441R( bitmap:Bitmap,dx:Int,dy:Int):Bitmap?{
        if(matrix==null){
           // Log.e("123"," defish 1944 matrix not init " )
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
        val result = Bitmap.createBitmap(DeFish1944.width1R1944, DeFish1944.width1R1944,Bitmap.Config.RGB_565)
        val outPixels = IntArray(DeFish1944.width1R1944 *DeFish1944. width1R1944)
    //    Log.e("123"," deFish19441R cretae time=${(System.currentTimeMillis()-t0)}  " )

        while( y <= dy+DeFish1944.width1R1944-1) {
            var x = dx
            while (x <=dx+DeFish1944.width1R1944-1 ){

                    val  xx= matrix!![x][y]
                    val  yy= matrixY!![x][y]
                    val colour = pixels[(yy.toInt()*BitmapWidth+xx.toInt())]
                    var outIndex=(y-dy)*DeFish1944.width1R1944+(x-dx)
                    if (outIndex<outPixels.size) outPixels[outIndex]=colour
                    else Log.e("123","deFish19441R outIndex="+outIndex);


                i += 1
                x += 1
            }
            y += 1
        }
        result.setPixels(outPixels, 0, DeFish1944.width1R1944, 0, 0,DeFish1944.width1R1944, DeFish1944.width1R1944)
        // Log.e("123","i="+i)
     //   Log.e("123"," defish 1R time=${(System.currentTimeMillis()-t0)}  " )
        return result
    }

    /* fun fisheye1P2Fix(fisheyeImage:UIImage,x:Int,y:Int) -> UIImage {

       // cgImage = fisheyeImage.cgImage
      //  provider = (cgImage?.dataProvider)!
       // providerData = provider?.data
      //  let img = CFDataGetBytePtr(providerData)//UnsafeMutablePointer(mutating: CFDataGetBytePtr(providerData))
       // NSLog("1280Fixend0")
     //   pixelDataOTemp2.withUnsafeBufferPointer { ptrToArray  in


                let leftOrright = x-pre2X
            let upOrdown = y-pre2Y

            var tempY = 0
            var partail = 0
            let a = Wd*(Hd/2+upOrdown)+Wd/2+leftOrright //1947 1286400 //301500
            var countY = 0
            while countY < 200 {
                var countX = 0
                while countX < 200 {
                    pixelsFix[partail] = PixelData(a: 255, r: img![ptrToArray[tempY+countX]], g: img![ptrToArray[tempY+countX]+1], b: img![ptrToArray[tempY+countX]+2])
                    countX += 1
                    partail += 1
                }
                countY += 1
                tempY = a + Wd2*countY
            }
            pre2X = x
            pre2Y = y
            // self.image = UIImage(pixels: pixels2, width: Wd2, height: Hd2)!
       // }
       // self.image = UIImage(pixels: pixelsFix, width: Wd2Fix, height: Hd2Fix)!
        //NSLog("1280Fixend1")
        return self.image
    }*/

    /*
    //1R
    var pre2X = Wd2/2
    var pre2Y = Hd2/2
    @objc func fisheye1P2Fix(fisheyeImage:UIImage,x:Int,y:Int) -> UIImage {
      //  return UIImage()
       // objc_sync_enter(fisheyeImage)
       // NSLog("start0")
//        ciImage = fisheyeImage.ciImage
//       // let ciImage = fisheyeImage
//
//        cgImage = ciContext.createCGImage(ciImage!, from: ciImage!.extent)
        cgImage = fisheyeImage.cgImage
        provider = (cgImage?.dataProvider)!
        providerData = provider?.data
        let img = CFDataGetBytePtr(providerData)//UnsafeMutablePointer(mutating: CFDataGetBytePtr(providerData))
       // NSLog("start1")
        NSLog("1280Fixend0")
        pixelDataOTemp2.withUnsafeBufferPointer { ptrToArray  in

//            var a = 0 //1947 1286400
//            print(ptrToArray.count)
//            while a < ptrToArray.count {
//
//                pixels2[a] = .init(a: 255, r: img![ptrToArray[a]], g: img![ptrToArray[a]+1], b: img![ptrToArray[a]+2])
//                a += 1
//            }
            let leftOrright = x-pre2X
            let upOrdown = y-pre2Y

            var tempY = 0
            var partail = 0
            let a = Wd2*(Hd2/2+upOrdown)+Wd2/2+leftOrright //1947 1286400 //301500
            var countY = 0
            while countY < 200 {
                var countX = 0
                while countX < 200 {
                    pixelsFix[partail] = PixelData(a: 255, r: img![ptrToArray[tempY+countX]], g: img![ptrToArray[tempY+countX]+1], b: img![ptrToArray[tempY+countX]+2])
                    countX += 1
                    partail += 1
                }
                countY += 1
                tempY = a + Wd2*countY
            }
            pre2X = x
            pre2Y = y
           // self.image = UIImage(pixels: pixels2, width: Wd2, height: Hd2)!
        }
        self.image = UIImage(pixels: pixelsFix, width: Wd2Fix, height: Hd2Fix)!
        NSLog("1280Fixend1")
        return self.image
    }
    * */

    /*
    var tempY = 0
            var partail = 0
            let a = Wd2*Hd2/2+Wd2/2 //1947 1286400 //301500
            var countY = 0
            while countY < 200 {
                var countX = 0
                while countX < 200 {
                    pixelsFix[partail] = PixelData(a: 255, r: img![ptrToArray[tempY+countX]], g: img![ptrToArray[tempY+countX]+1], b: img![ptrToArray[tempY+countX]+2])
                    countX += 1
                    partail += 1
                }
                countY += 1
                tempY = a + Wd2*countY
            }
    */
    /*
    @objc func fisheye1P2(fisheyeImage:UIImage) -> UIImage {

            cgImage = fisheyeImage.cgImage
            provider = (cgImage?.dataProvider)!
            providerData = provider?.data
            let img = CFDataGetBytePtr(providerData)//UnsafeMutablePointer(mutating: CFDataGetBytePtr(providerData))
            NSLog("1280end0")
            pixelDataOTemp2.withUnsafeBufferPointer { ptrToArray  in
                var a = 0
                while a < ptrToArray.count {
                    pixels2[a] = PixelData(a: 255, r: img![ptrToArray[a]], g: img![ptrToArray[a]+1], b: img![ptrToArray[a]+2])

                   a += 1
                }
            }
            self.image = UIImage(pixels: pixels2, width: Wd2, height: Hd2)!
            NSLog("1280end1")
            return self.image

        }
     */
}