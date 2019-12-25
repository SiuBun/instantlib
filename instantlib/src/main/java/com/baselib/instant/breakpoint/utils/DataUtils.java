package com.baselib.instant.breakpoint.utils;

import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 数据处理相关工具类
 *
 * @author wsb
 */
public class DataUtils {
    /**
     * 获取重定向处理后的url
     *
     * @param sourceUrl 源url
     * @return 重定向后的url
     */
    public static String getRedirectionUrl(String sourceUrl) throws Exception {

        String redirectUrl = null;

        URL url = new URL(sourceUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        conn.connect();
        if (conn.getResponseCode() == BreakPointConst.REQ_REFLECT) {
            redirectUrl = conn.getHeaderField("Location");
            LogUtils.i(" 该链接出现重定向,下载地址重定向为 " + redirectUrl);
        } else {
            LogUtils.i(" 下载地址请求码 = " + conn.getResponseCode());

        }
        conn.disconnect();

        return redirectUrl;
    }

    public static String spliceTaskFileDir(File file) {
        return file.getAbsolutePath() + File.separator + BreakPointConst.DIR_NAME;
    }

    public static int generateId(final String url, final String path) {
        return md5(formatString("%sp%s", url, path)).hashCode();
    }

    private static String formatString(final String msg, Object... args) {
        return String.format(Locale.ENGLISH, msg, args);
    }

    private static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * 获取分段任务理论下载终点
     *
     * @param contentLength 文件长度
     * @param threadId      线程下标
     */
    public static long getTheoryStartIndex(long contentLength, int threadId) {
        return contentLength / BreakPointConst.DEFAULT_THREAD_COUNT * threadId;
    }

    /**
     * 获取分段任务理论下载终点
     *
     * @param contentLength 文件长度
     * @param threadId      线程下标
     */
    public static long getTheoryEndIndex(long contentLength, int threadId) {
        final long segmentSize = contentLength / BreakPointConst.DEFAULT_THREAD_COUNT;
        long endIndex;
        // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
        if (threadId == BreakPointConst.DEFAULT_THREAD_COUNT - 1) {
            endIndex = contentLength;
        } else {
            endIndex = (threadId + 1) * segmentSize;
        }
        endIndex = endIndex - 1;
        return endIndex;
    }
}
