package pers.ignatius.bilibili.core;

import pers.ignatius.bilibili.exception.FileUnexpectedEndException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName : TransCoding
 * @Description : 负责视频转码等处理
 * @author IgnatiusGL
 * @Date : 2020-02-28 15:58
 */
public class TransCoding {
    private static String ffmpeg;
    private volatile static int gpuNum;
    private static final Object gpuLocker = new Object();

    private static void gpuHandle(){
        while (true){
            //获取当前GPU转码数量
            synchronized (TransCoding.gpuLocker){
                if (gpuNum < 2) {
                    gpuNum++;
                    return;
                }
            }
            //休息一下
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(Thread.currentThread().getName() + "\t线程错误");
            }
        }
    }

    private String videoUrl;
    private String audioUrl;
    private String tvideoUrl;
    private String taudioUrl;

    /**
     * 创建转码程序
     * @param path   地址
     */
    public static void buildProgram(String path){
        try {
            InputStream is = TransCoding.class.getResource("/ffmpeg/bin/ffmpeg.exe").openStream();
            ffmpeg = path + "\\ffmpeg.exe";
            if (!new File(ffmpeg).exists()){
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
    public static void disBuildProgram(){
        new File(ffmpeg).delete();
    }

    /**
     * 处理m4s文件
     * @param ms4Url m4s文件地址
     * @param path  处理后生成文件地址
     * @return  true-成功 false-失败
     */
    public boolean transM4s(String ms4Url,String path,Progress progress,boolean isTryHardwareDecoding) throws FileUnexpectedEndException {
        System.out.println(Thread.currentThread().getName() + "\t开始转码");
        //保存路径,方便清理
        String fileEnd = path.substring(path.length() - 3);
        if ("mp4".equals(fileEnd)){
            this.videoUrl = ms4Url;
            this.tvideoUrl = path;
        }else if ("mp3".equals(fileEnd)){
            this.audioUrl = ms4Url;
            this.taudioUrl = path;
        }else {
            throw new FileUnexpectedEndException(fileEnd);
        }

        //清理目标文件
        File file = new File(path);
        if (file.exists()) {
            System.out.println(Thread.currentThread().getName() + "\t目标文件存在 " + path);
            file.delete();
        }

        //项目utf-8 命令行gbk 转码
        try {
            ms4Url = new String(ms4Url.getBytes( "GBK"),"GBK");
            path = new String(path.getBytes( "GBK"),"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        //命令
        String command;
        //硬件加速只应用于视频转码
        if (isTryHardwareDecoding && !"mp3".equals(fileEnd)){
            System.out.println(Thread.currentThread().getName() + "\t使用硬件加速");
            TransCoding.gpuHandle();
            command =  ffmpeg +
                    " -i " +
                    ms4Url +
                    " -vcodec h264_nvenc -f " +
                    fileEnd +
                    " " +
                    path;
        }else {
            System.out.println(Thread.currentThread().getName() + "\t不使用硬件加速");
            command =  ffmpeg +
                    " -i " +
                    ms4Url +
                    " -strict -2 -c:v libx264 -f " +
                    fileEnd +
                    " " +
                    path;
        }
        boolean result = exec(Arrays.asList(command.split(" ")),progress);

        //释放GPU请求
        if ("mp4".equals(fileEnd)) {
            synchronized (TransCoding.gpuLocker) {
                TransCoding.gpuNum--;
            }
        }
        return result;
    }

    /**
     * 合并视频和音频
     * @param videoUrl  视频地址
     * @param audioUrl  音频地址
     * @param path  生成视频路径
     * @return  true-成功 false-失败
     */
    public boolean merge(String videoUrl,String audioUrl,String path,Progress progress){
        System.out.println(Thread.currentThread().getName() + "\t合并文件");
        //清理目标文件
        File file = new File(path);
        if (file.exists()) {
            System.out.println(Thread.currentThread().getName() + "\t目标文件存在 " + path);
            file.delete();
        }

        //项目utf-8 命令行gbk 转码
        try {
            videoUrl = new String(videoUrl.getBytes( "GBK"),"GBK");
            audioUrl = new String(audioUrl.getBytes( "GBK"),"GBK");
            path = new String(path.getBytes( "GBK"),"GBK");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        String command =  ffmpeg +
                " -i " + videoUrl +
                " -i " + audioUrl +
                " -c:v copy -c:a aac -strict experimental " + path;
        return exec(Arrays.asList(command.split(" ")),progress);
    }

    /**
     * 清理临时文件
     */
    public void cleanTmpFile(){
        new File(videoUrl).delete();
        new File(audioUrl).delete();
        new File(tvideoUrl).delete();
        new File(taudioUrl).delete();
    }

    /**
     * 执行程序
     * @param command   命令
     * @return  true-成功 false-失败
     */
    private boolean exec(List<String> command,Progress progress){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try {
            System.out.println(Thread.currentThread().getName() + "\t开始处理");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            new StreamHandler(process.getInputStream(),progress).start();
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
        private Progress progress;

        public StreamHandler(InputStream in,Progress progress) {
            this.in = in;
            this.progress = progress;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                long totalTime = 1;
                long currentTime = 0;
                while ((line = reader.readLine()) != null) {
                    //获取总时间
                    if (totalTime == 1){
                        //Duration: 00:00:21.93, start: 0.000000, bitrate: 387 kb/s
                        String t = findText("Duration: \\d\\d:\\d\\d:\\d\\d.\\d\\d,",line);
                        if (t != null){
                            String[] times = t.substring(10, t.length()-1).split("[:.]");
                            //计算秒毫秒舍去
                            totalTime = 0;
                            for (int i=1;i<times.length;i++){
                                totalTime += Long.parseLong(times[times.length - i - 1]) * (int)Math.pow(60, i-1);
                            }
                            totalTime--;
                        }
                    }
                    String y = findText("time=\\d\\d:\\d\\d:\\d\\d.\\d\\d bitrate=",line);
                    if (y != null){
                        String[] times = y.substring(5, y.length()-9).split("[:.]");
                        //计算秒毫秒舍去
                        currentTime = 0;
                        for (int i=1;i<times.length;i++){
                            currentTime += Long.parseLong(times[times.length - i - 1]) * (int)Math.pow(60, i-1);
                        }
                    }
                    progress.setProgress((double) currentTime/totalTime);
                    System.out.println(Thread.currentThread().getName() + "\t" + line);
                }
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + "\t" + e.getMessage());
            }
        }
        /**
         * 正则匹配找第一个
         * @param reger 正则表达式
         * @param page  匹配的页面
         * @return  匹配的文字
         */
        private String findText(String reger,String page){
            Pattern pattern = Pattern.compile(reger);
            Matcher matcher = pattern.matcher(page);
            if(matcher.find()){
                return matcher.group(0);
            }
            return null;
        }
    }


}
