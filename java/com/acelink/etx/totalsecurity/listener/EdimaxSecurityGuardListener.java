package com.acelink.etx.totalsecurity.listener;

public interface EdimaxSecurityGuardListener
{
    void tsCommandCallback(String deviceId,
                                int job,
                                int customId,
                                byte[] content,
                                int contentLength,
                                int status);
    void tsVideoCallback(String deviceId, byte[] content, int contentLength, int format,
                                int iFrame, int width, int height, long playTimeMs);

     void tsAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs);

    void tsMessageCallback(int type, String message);
}
