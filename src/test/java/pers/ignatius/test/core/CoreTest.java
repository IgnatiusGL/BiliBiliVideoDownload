package pers.ignatius.test.core;

import org.junit.Test;
import pers.ignatius.bilibili.core.*;
import pers.ignatius.bilibili.exception.AnalyzeUrlException;
import pers.ignatius.bilibili.exception.InternetException;
import pers.ignatius.bilibili.exception.UnknownException;
import pers.ignatius.bilibili.exception.WebsiteNotEndWithAvException;

import java.io.File;
import java.util.List;

public class CoreTest {
    private File file;

    {
//        file = new File(getClass().getResource("/").getPath());
//        file = new File(file.getParentFile().getParentFile().getParentFile().getParentFile() + "\\download");
    }

    @Test
    public void testDownload(){
        Download download = new Download();
        String url = "http://cn-jlcc-cu-v-01.bilivideo.com/upgcxcode/62/12/153371262/153371262-1-30080.m4s?expires=1582880400&platform=pc&ssig=8BOIOJpm1oF46kTaqH2A_A&oi=2947505655&trid=e4e39bbf94a64bb58f3d1bf06105a2a1u&nfc=1&nfb=maPYqpoel5MI3qOUX6YpRA==&mid=28134940";
        String avId = "89799412";
        String path =  file + "\\newVideo.m4s";
//        download.download(url, path, avId, DownloadType.VIDEO);
        url = "http://cn-hbcd2-cu-v-16.bilivideo.com/upgcxcode/62/12/153371262/153371262-1-30280.m4s?expires=1582881300&platform=pc&ssig=bFif2VpDF_PJj32voLzvGw&oi=2947505655&trid=68ebd91d6991410e8a147e0dafa6e651u&nfc=1&nfb=maPYqpoel5MI3qOUX6YpRA==&mid=28134940&logo=80000000";
        path =  file + "\\newAudio.m4s";
//        download.download(url, path, avId, DownloadType.AUDIO);
    }

    @Test
    public void testTransCoding(){
        TransCoding transCoding = new TransCoding(file.toString());
//        transCoding.transM4s(file + "\\newVideo.m4s",file + "\\newVideo.mp4");
//        transCoding.transM4s(file + "\\newAudio.m4s",file + "\\newAudio.mp3");
    }

    @Test
    public void merge(){
        TransCoding transCoding = new TransCoding(file.toString());
//        transCoding.merge(file + "\\newVideo.mp4",file + "\\newAudio.mp3",file + "\\resultVideo.mp4");
    }

    @Test
    public void testUrl() throws AnalyzeUrlException, WebsiteNotEndWithAvException, InternetException, UnknownException {
        GetVideoInformation getVideoInformation = new GetVideoInformation();
//        List<VideoInformation> list = getVideoInformation.getVideoInformationFromUrl("https://www.bilibili.com/video/av89799412",VideoQuality.P360);
        List<VideoInformation> list = getVideoInformation.getVideoInformationFromUrl("https://www.bilibili.com/video/av59611545",VideoQuality.P360);
        for (VideoInformation v:list){
            System.out.println(v);
        }
    }

    @Test
    public void testWohleOperate() throws InterruptedException {
//        GetVideoInformation getVideoInformation = new GetVideoInformation();
//        Download download = new Download();
//        TransCoding transCoding = new TransCoding(file.toString());
//        getVideoInformation.analyzeUrl("https://www.bilibili.com/video/av90748215", VideoQuality.FHD);
//        String videoUrl = file + "/" + getVideoInformation.getTitle() + "Video.ms4";
//        String audioUrl = file + "/" + getVideoInformation.getTitle() + "Audio.ms4";
//        String tvideoUrl = file + "/" + getVideoInformation.getTitle() + "Video.mp4";
//        String taudioUrl = file + "/" + getVideoInformation.getTitle() + "Audio.mp3";
//        String resultUrl = file + "/" + getVideoInformation.getTitle() + ".mp4";
//        download.download(getVideoInformation.getVideoUrl(),videoUrl,getVideoInformation.getAv(), DownloadType.VIDEO);
//        download.download(getVideoInformation.getAudioUrl(),audioUrl,getVideoInformation.getAv(), DownloadType.AUDIO);
//        transCoding.transM4s(videoUrl,tvideoUrl);
//        transCoding.transM4s(audioUrl,taudioUrl);
//        transCoding.merge(tvideoUrl,taudioUrl,resultUrl);
//        System.out.println("完成");
//        Thread.sleep(5000);
//        new File(videoUrl).delete();
//        new File(audioUrl).delete();
//        new File(tvideoUrl).delete();
//        new File(taudioUrl).delete();
    }
}
