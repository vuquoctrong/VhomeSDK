package com.vht.sdkcore.utils;



import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class UtilsJava {


    public static void int2buff(byte[] buff, int size, int val)
    {
        size = size > 4? 4 : size;
        for (int i = 0; i < size; i ++)
        {
            buff[i] = (byte)(val & 0x000000FF);
            val = val >> 8;
        }
    }
    public static int buff2Int(byte[] buff, int size)
    {
        size = size > 4? 4 : size;
        int val = 0;
        for (int i = 0; i < size; i ++)
        {
            val |= (((int) buff[i] & 0x000000FF) << (i*8));
        }
        return val;
    }


    public static void int2Bytes(byte[] bytes, int size, int val)
    {
        size = size > 4? 4 : size;
        for (int i = 0; i < size; i ++)
        {
            bytes[i] = (byte)(val & 0x000000FF);
            val = val >> 8;
        }
    }
    public static int bytes2Int(byte[] bytes, int size)
    {
        size = size > 4? 4 : size;
        int val = 0;
        for (int i = 0; i < size; i ++)
        {
            val |= (((int) bytes[i] & 0x000000FF) << (i*8));
        }
        return val;
    }

    public static String convertToTimeSecond(Long callSecond) {
        if (callSecond == null) {
            return "00:00";
        }
        long oneMinuteSecond = TimeUnit.MINUTES.toSeconds(1);

        int minute = (int) (callSecond / oneMinuteSecond);
        int seconds = (int) (callSecond % 60);
        return String.format("%02d:%02d", minute, seconds);
    }

    public static String formatBitrate(long size) {
        DecimalFormat df = new DecimalFormat("#.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        String fileSizeString = "";
        String wrongSize = "0 KB/s";
        if (size == 0L) {
            return wrongSize;
        } else {
            if (size < 1024L) {
                fileSizeString = df.format((double)size) + " B/s";
            } else if (size < 1048576L) {
                fileSizeString = df.format((double)size / 1024.0D) + " KB/s";
            } else if (size < 1073741824L) {
                fileSizeString = df.format((double)size / 1048576.0D) + " MB/s";
            } else {
                fileSizeString = df.format((double)size / 1.073741824E9D) + " GB/s";
            }

            return fileSizeString;
        }
    }
}
