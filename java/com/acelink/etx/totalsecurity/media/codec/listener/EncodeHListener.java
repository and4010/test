package com.acelink.etx.totalsecurity.media.codec.listener;

/**
 * Created by Gregory on 2016/12/6.
 */
public interface EncodeHListener {

    void onAudioEncode(byte[] data);
    void onAudioStop();
    boolean isStartFirst();
}
