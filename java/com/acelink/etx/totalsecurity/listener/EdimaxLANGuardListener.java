package com.acelink.etx.totalsecurity.listener;

public interface EdimaxLANGuardListener
{
    void LifeLanMainCallback(String deviceId,
                                int job,
                                int customId,
                                byte[] content,
                                int contentLength,
                                int status);
    void LifeLanVideoCallback(String deviceId, byte[] content, int contentLength, int format,
                                int iFrame, int width, int height, long playTimeMs);

     void LifeLanAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs);

    void LifeLanMsgCallback(int type, String message);
}
