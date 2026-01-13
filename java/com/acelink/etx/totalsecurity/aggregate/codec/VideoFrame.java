package com.acelink.etx.totalsecurity.aggregate.codec;

public class VideoFrame {
   public byte[] content;
    public int lengtht;
   int iFrame;

   public VideoFrame(byte[] c, int l, int f){
       content=c;
       lengtht=l;
       iFrame=f;
   }
}
