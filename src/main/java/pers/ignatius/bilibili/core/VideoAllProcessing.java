package pers.ignatius.bilibili.core;

import pers.ignatius.bilibili.exception.FileUnexpectedEndException;

/**
 * @ClassName : VideoAllProcessing
 * @Description : 集成视频的所有操作
 * @Author : IgnatiusGL
 * @Date : 2020-03-02 16:04
 */
public class VideoAllProcessing implements Runnable {
    private VideoInformation videoInformation;
    private Download download;
    private TransCoding transCoding;
    private String path;
    private String message = "";
    private Progress downloadVideoProgress;
    private Progress downloadAudioProgress;
    private Progress transCodeVideoProgress;
    private Progress transCodeAudioProgress;
    private Progress mergeProgress;
    private boolean isTryHardwareDecoding;
    private ProgressChangeAction progressChangeAction;

    {
        download = new Download();
        downloadVideoProgress = new Progress();
        downloadAudioProgress = new Progress();
        transCodeVideoProgress = new Progress();
        transCodeAudioProgress = new Progress();
        mergeProgress = new Progress();
    }

    public VideoAllProcessing(VideoInformation videoInformation, String path,boolean isTryHardwareDecoding) {
        this.videoInformation = videoInformation;
        this.path = path;
        this.isTryHardwareDecoding = isTryHardwareDecoding;
        transCoding = new TransCoding();
        //设置监听器
        ProgressChangeAction pca = () -> {
            if (progressChangeAction != null)
                this.progressChangeAction.changeAction();
        };
        downloadVideoProgress.setProgressChangeAction(pca);
        downloadAudioProgress.setProgressChangeAction(pca);
        transCodeVideoProgress.setProgressChangeAction(pca);
        transCodeAudioProgress.setProgressChangeAction(pca);
        mergeProgress.setProgressChangeAction(pca);
    }

    @Override
    public void run() {
        String videoUrl = path + "\\" + videoInformation.getTitle() + "Video.m4s";
        String audioUrl = path + "\\" + videoInformation.getTitle() + "Audio.m4s";
        String tvideoUrl = path + "\\" + videoInformation.getTitle() + "Video.mp4";
        String taudioUrl = path + "\\" + videoInformation.getTitle() + "Audio.mp3";
        String resultUrl = path + "\\" + videoInformation.getTitle() + ".mp4";
        //下载
        setMessage("正在下载视频");
        download.download(videoInformation.getVideoUrl(),videoInformation.getUrl(), videoUrl, downloadVideoProgress);
        download.download(videoInformation.getAudioUrl(),videoInformation.getUrl(), audioUrl, downloadAudioProgress);
        //转码
        setMessage("正在转码,时间较长");
        try {
            transCoding.transM4s(videoUrl,tvideoUrl,transCodeVideoProgress,isTryHardwareDecoding);
            transCoding.transM4s(audioUrl,taudioUrl,transCodeAudioProgress,isTryHardwareDecoding);
        } catch (FileUnexpectedEndException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            transCoding.cleanTmpFile();
            return;
        }
        transCoding.merge(tvideoUrl,taudioUrl,resultUrl,mergeProgress);
        //清理
        transCoding.cleanTmpFile();
        if ("100%".equals(getProgress().toString())){
            setMessage("完成");
        }else {
            setMessage("异常");
        }
    }

    /**
     * 设置进度监听器
     * @param progressChangeAction  进度监听器
     */
    public void setProgressChangeAction(ProgressChangeAction progressChangeAction) {
        this.progressChangeAction = progressChangeAction;
    }

    public void setMessage(String message){
        this.message = message;
        progressChangeAction.changeAction();
    }

    /**
     * 获取当前提示消息
     * @return 提示信息
     */
    public String getMessage(){
        return message;
    }

    /**
     * 获取进度
     * @return  进度0-1
     */
    public Progress getProgress(){
        return new Progress(downloadVideoProgress.getProgress()*0.15 +
                downloadAudioProgress.getProgress()*0.05 +
                transCodeVideoProgress.getProgress()*0.70 +
                transCodeAudioProgress.getProgress()*0.05 +
                mergeProgress.getProgress()*0.05);
    }
}
