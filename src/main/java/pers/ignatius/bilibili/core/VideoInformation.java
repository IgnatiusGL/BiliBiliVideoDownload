package pers.ignatius.bilibili.core;

/**
 * @ClassName : VideoInformation
 * @Description : 存储分析网页视频的信息
 * @Author : IgnatiusGL
 * @Date : 2020-03-01 21:55
 */
public class VideoInformation {
    private String videoUrl;//视频url
    private String audioUrl;//音频url
    private String title;//标题
    private String url;//地址

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "VideoInformation{" +
                "videoUrl='" + videoUrl + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
