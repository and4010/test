package com.acelink.etx.totalsecurity.media.codec;

import java.nio.ByteBuffer;

public class StreamContent {
    private ByteBuffer content;
    private long timeStand=0;

    public StreamContent(byte[] content,int length) {
        this.content = ByteBuffer.wrap(content,0,length);

    }

    public ByteBuffer getContent() {
        return content;
    }

    public long getTimeStand() {
        return timeStand;
    }

    public void setTimeStand(long timeStand) {
        this.timeStand = timeStand;
    }
}
