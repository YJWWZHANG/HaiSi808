package com.guoyee.haisi808;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public final class ConvertUtilsPlus {

    private ConvertUtilsPlus() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    //公共计算CRC校验的函数
    private static int mCrcTab[] =
            { // X16+X12+X5+1 余式表
                    0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                    0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
                    0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
                    0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
                    0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
                    0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
                    0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
                    0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
                    0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
                    0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
                    0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
                    0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
                    0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
                    0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
                    0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
                    0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
                    0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
                    0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
                    0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
                    0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
                    0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
                    0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
                    0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
                    0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
                    0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
                    0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
                    0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
                    0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
                    0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
                    0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
                    0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
                    0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
            };

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[byteRet.length - 1 - i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[arr.length - 1 - i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    public static int getCrc16(byte[] bufData, int offset, int buflen) {
        byte dat;
        short crc;
        int l;
        l = buflen;
        crc = 0;
        int off = offset;
        while (l-- != 0) {
            dat = (byte) (crc >> 8);
            crc <<= 8;
            short re = byte2UnsignedShort((byte) (dat ^ bufData[off]));
            if (re < 0 || re > 256) {
                LogUtils.e("crc index is illegality!");
            } else {
                crc ^= mCrcTab[re];
            }

            off++;
        }
        return short2Unsigned(crc);
    }

    public static byte[] int2ByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static byte getXOR(byte[] bytes) {
        byte b = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            b = (byte) (b ^ bytes[i]);
        }
        return b;
    }

    public static byte getXOR(ArrayList<Byte> list) {
        byte b = list.get(1);
        for (int i = 2; i < list.size() - 2; i++) {
            b = (byte) (b ^ list.get(i));
        }
        return b;
    }

    public static short byte2UnsignedShort(byte s) {
        return (short) (s & 0xFF);
    }

    public static int short2Unsigned(short s) {
        return s & 0xFFFF;
    }

    /**
     * 读取流作为字符串返回
     *
     * @param is 流
     * @return
     * @throws IOException
     */
    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {// 等于-1时，读取完毕
            baos.write(buffer, 0, len);
        }
        String result = baos.toString("utf-8");
        is.close();
        baos.close();
        return result;
    }

    /**
     * 读取Asset目录下的资源
     *
     * @param assetsFileName 要读取的文件名
     * @return 文件内容字符串
     */
    public static String AssetsFile2String(String assetsFileName) {
        try {
            InputStream fis = Utils.getApp().getAssets().open(assetsFileName);
            return inputStream2String(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void reverseArray(byte[] array) {
        for (int start = 0, end = array.length - 1; start < end; start++, end--) {
            byte temp = array[end];
            array[end] = array[start];
            array[start] = temp;
        }
    }

    //根据秒数转化为时分秒   00:00:00
    public static String getStringTime(long cnt) {
        long hour = cnt / 3600;
        long min = cnt % 3600 / 60;
        long second = cnt % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, min, second);
    }

    public static void encrypt(long k, long a, long b, long c, byte[] data, int size) {
        long Key, M1, IA1, IC1;
        int idx = 0;
        Key = k;
        M1 = a;
        IA1 = b;
        IC1 = c;

        if (Key == 0) Key = 1;
        if (M1 == 0) M1 = 1;
        if (IA1 == 0) IA1 = 1;
        if (IC1 == 0) IC1 = 1;
        while (idx < size) {
            Key = IA1 * (Key % M1) + IC1;
            data[idx++] ^= (byte) ((Key >> 20) & 0xFF);
        }
    }


}
