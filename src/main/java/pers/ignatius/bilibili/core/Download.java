package pers.ignatius.bilibili.core;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author IgnatiusGL
 * @ClassName : Download
 * @Description : 下载类,负责下载资源
 * @Date : 2020-02-28 15:58
 */
public class Download {
    /**
     * 下载资源
     *
     * @param url      地址
     * @param path     保存路径
     * @param progress 进度
     * @return true-下载成功 false-下载失败
     */
    public boolean download(String url, String referer, String path, Progress progress) {
        //连接
        HttpURLConnection httpURLConnection = null;
        try {
            // 连接失败重试
            boolean flag = true;
            while (flag) {
                try {
                    httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestProperty("referer", referer);
                    httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
                    httpURLConnection.connect();
                    flag = false;
                } catch (SSLHandshakeException e) {
                    System.out.println("Download(40):javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "\t网络连接错误");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //下载文件
        int fileLength = httpURLConnection.getContentLength();
        fileLength--;
        BufferedInputStream bin;
        try {
            bin = new BufferedInputStream(httpURLConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        OutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        int size = 0;
        int len = 0;
        byte[] buf = new byte[1024];
        try {
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                progress.setProgress((double) len / fileLength);
            }
            bin.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t下载完毕");
        return true;
    }
}
