package pers.ignatius.bilibili.core;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName : TransCoding
 * @Description : 负责视频转码等处理
 * @author IgnatiusGL
 * @Date : 2020-02-28 15:58
 */
public class TransCoding {
    private String ffmpeg;
    private String videoUrl;
    private String audioUrl;
    private String tvideoUrl;
    private String taudioUrl;

    public TransCoding(String url){
        try {
            InputStream is = getClass().getResource("/ffmpeg/bin/ffmpeg.exe").openStream();
            ffmpeg = url + "/ffmpeg.exe";
            OutputStream os = new FileOutputStream(ffmpeg);
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {

                os.write(b, 0, length);

            }
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理m4s文件
     * @param ms4Url m4s文件地址
     * @param path  处理后生成文件地址
     * @return  true-成功 false-失败
     */
    public boolean transM4s(String ms4Url,String path){
        //保存路径,方便清理
        if (path.contains(".mp4")){
            this.videoUrl = ms4Url;
            this.tvideoUrl = path;
        }else if (path.contains(".mp3")){
            this.audioUrl = ms4Url;
            this.taudioUrl = path;
        }
        //项目utf-8 命令行gbk 转码
        try {
            ms4Url = new String(ms4Url.getBytes( "GBK"),"GBK");
            path = new String(path.getBytes( "GBK"),"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        String command =  ffmpeg +
                " -i " + ms4Url +
                " -c:v libx264 -strict -2 " + path;
        return exec(Arrays.asList(command.split(" ")));
    }

    /**
     * 合并视频和音频
     * @param videoUrl  视频地址
     * @param audioUrl  音频地址
     * @param path  生成视频路径
     * @return  true-成功 false-失败
     */
    public boolean merge(String videoUrl,String audioUrl,String path){
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
        return exec(Arrays.asList(command.split(" ")));
    }

    /**
     * 清理临时文件
     */
    public void cleanTmpFile(){
        new File(videoUrl).delete();
        new File(audioUrl).delete();
        new File(tvideoUrl).delete();
        new File(taudioUrl).delete();
        new File(ffmpeg).delete();
    }

    /**
     * 执行程序
     * @param command   命令
     * @return  true-成功 false-失败
     */
    private boolean exec(List<String> command){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try {
            System.out.println("开始转码处理");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder sbf = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
