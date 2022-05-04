package pers.ignatius.bilibili.core;

import pers.ignatius.bilibili.exception.FileUnexpectedEndException;

import javax.net.ssl.SSLHandshakeException;

/**
 * @ClassName : VideoAllProcessing
 * @Description : 集成视频的所有操作
 * @Author : IgnatiusGL
 * @Date : 2020-03-02 16:04
 */
public class VideoAllProcessing implements Runnable {
    private final VideoInformation videoInformation;
    private final Download download;
    private final TransCoding transCoding;
    private final String path;
    private final Progress downloadVideoProgress;
    private final Progress downloadAudioProgress;
    private final Progress transProgress;
    private String message = "";
    private ProgressChangeAction progressChangeAction;

    {
        download = new Download();
        downloadVideoProgress = new Progress();
        downloadAudioProgress = new Progress();
        transProgress = new Progress();
    }

    public VideoAllProcessing(VideoInformation videoInformation, String path) {
        this.videoInformation = videoInformation;
        this.path = path;
        transCoding = new TransCoding();
        //设置监听器
        ProgressChangeAction pca = () -> {
            if (progressChangeAction != null)
                this.progressChangeAction.changeAction();
        };
        downloadVideoProgress.setProgressChangeAction(pca);
        downloadAudioProgress.setProgressChangeAction(pca);
        transProgress.setProgressChangeAction(pca);
    }

    @Override
    public void run() {
        String videoUrl = path + "\\" + videoInformation.getTitle() + "Video.m4s";
        String audioUrl = path + "\\" + videoInformation.getTitle() + "Audio.m4s";
        String resultUrl = path + "\\" + videoInformation.getTitle() + ".mp4";
        //下载
        setMessage("正在下载视频");
        download.download(videoInformation.getVideoUrl(), videoInformation.getUrl(), videoUrl, downloadVideoProgress);
        download.download(videoInformation.getAudioUrl(), videoInformation.getUrl(), audioUrl, downloadAudioProgress);
        //转码
        setMessage("正在转码");
        try {
            transCoding.transM4s(videoUrl, audioUrl, resultUrl, transProgress);
        } catch (FileUnexpectedEndException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            transCoding.cleanTmpFile();
            return;
        }
        //清理
        transCoding.cleanTmpFile();
        if ("100%".equals(getProgress().toString())) {
            setMessage("完成");
        } else {
            setMessage("异常");
        }
    }

    /**
     * 设置进度监听器
     *
     * @param progressChangeAction 进度监听器
     */
    public void setProgressChangeAction(ProgressChangeAction progressChangeAction) {
        this.progressChangeAction = progressChangeAction;
    }

    public void setMessage(String message) {
        this.message = message;
        progressChangeAction.changeAction();
    }

    /**
     * 获取当前提示消息
     *
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取进度
     *
     * @return 进度0-1
     */
    public Progress getProgress() {
        return new Progress(downloadVideoProgress.getProgress() * 0.90 +
                downloadAudioProgress.getProgress() * 0.09 +
                transProgress.getProgress() * 0.01);
    }
}
