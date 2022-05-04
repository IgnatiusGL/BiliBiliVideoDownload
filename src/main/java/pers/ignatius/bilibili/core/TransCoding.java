package pers.ignatius.bilibili.core;

import pers.ignatius.bilibili.exception.FileUnexpectedEndException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IgnatiusGL
 * @ClassName : TransCoding
 * @Description : 负责视频转码等处理
 * @Date : 2020-02-28 15:58
 */
public class TransCoding {
    private static String ffmpeg;

    private String videoUrl;
    private String audioUrl;


    /**
     * 创建转码程序
     *
     * @param path 地址
     */
    public static void buildProgram(String path) {
        try {
            InputStream is = TransCoding.class.getResource("/ffmpeg/bin/ffmpeg.exe").openStream();
            ffmpeg = path + "\\ffmpeg.exe";
            if (!new File(ffmpeg).exists()) {
                OutputStream os = new FileOutputStream(ffmpeg);
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除转码程序
     */
    public static void disBuildProgram() {
        new File(ffmpeg).delete();
    }

    /**
     * 处理m4s文件
     *
     * @param videoMs4Url  m4s文件视频地址
     * @param audioM4sUrl m4s文件音频地址
     * @param path         处理后生成文件地址
     * @return true-成功 false-失败
     */
    public boolean transM4s(String videoMs4Url, String audioM4sUrl, String path, Progress transProgress) throws FileUnexpectedEndException {
        System.out.println(Thread.currentThread().getName() + "\t开始转码");

        //保存路径，以便清理
        videoUrl = videoMs4Url;
        audioUrl = audioM4sUrl;

        //清理目标文件
        File file = new File(path);
        if (file.exists()) {
            System.out.println(Thread.currentThread().getName() + "\t目标文件存在 " + path);
            file.delete();
        }

        //项目utf-8 命令行gbk 转码
        try {
            videoMs4Url = new String(videoMs4Url.getBytes("GBK"), "GBK");
            audioM4sUrl = new String(audioM4sUrl.getBytes("GBK"), "GBK");
            path = new String(path.getBytes("GBK"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        //命令
        String command = ffmpeg +
                " -i " +
                videoMs4Url +
                " -i " +
                audioM4sUrl +
                " -codec copy " +
                path;
        boolean result = exec(Arrays.asList(command.split(" ")));
        if (result) {
            transProgress.setProgress(100);
        }
        return result;
    }

    /**
     * 清理临时文件
     */
    public void cleanTmpFile() {
        new File(videoUrl).delete();
        new File(audioUrl).delete();
    }

    /**
     * 执行程序
     *
     * @param command 命令
     * @return true-成功 false-失败
     */
    private boolean exec(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try {
            System.out.println(Thread.currentThread().getName() + "\t开始处理");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            new StreamHandler(process.getInputStream()).start();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 处理子进程输出的线程，防止死锁
    static class StreamHandler extends Thread {

        private InputStream in;

        public StreamHandler(InputStream in) {
            this.in = in;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    //获取总时间
                    System.out.println(Thread.currentThread().getName() + "\t" + line);
                }
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + "\t" + e.getMessage());
            }
        }
    }


}
