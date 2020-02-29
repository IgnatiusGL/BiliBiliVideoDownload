package pers.ignatius.bilibili.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pers.ignatius.bilibili.core.*;

import java.io.File;

/**
 * @ClassName : MainController
 * @Description : 主界面的控制
 * @Author : IgnatiusGL
 * @Date : 2020-02-28 22:39
 */
public class MainController {
    private GetVideoInformation getVideoInformation = new GetVideoInformation();

    @FXML
    private ChoiceBox choiceBox;

    @FXML
    public void initialize(){
        choiceBox.setItems(FXCollections.observableArrayList("1080P","720P","480P"));
    }

    @FXML
    private TextField videoUrl;
    @FXML
    private Label p1Title;
    @FXML
    private ProgressBar p1Progress;
    @FXML
    private Label p1ProgressPercent;
    @FXML
    private Label p1PercentInformation;
    @FXML
    private Button start;


    @FXML
    protected void buttonAnalyseOnClick(ActionEvent actionEvent) {
        System.out.println();
        VideoQuality quality;
        String t = choiceBox.getValue().toString();
        if ("1080P".equals(t))
            quality = VideoQuality.FHD;
        else if ("720P".equals(t))
            quality = VideoQuality.HD;
        else
            quality = VideoQuality.SD;
        getVideoInformation.analyzeUrl(videoUrl.getText(),quality);
        p1Title.setText(getVideoInformation.getTitle());
        p1Progress.setVisible(true);
        p1ProgressPercent.setVisible(true);
        p1PercentInformation.setText("准备下载");
        start.setDisable(false);
    }
    @FXML
    private TextField path;



    @FXML
    private void buttonSelectPathOnClick(ActionEvent actionEvent){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择视频保存路径");
        File selectedFile = directoryChooser.showDialog(new Stage());
        if(selectedFile == null)
            return;
        String path = selectedFile.getPath();
        this.path.setText(path);
    }

    private Download download;

    @FXML
    private void buttonStartOnClick(ActionEvent actionEvent){
        String videoUrl = path.getText() + "\\" + getVideoInformation.getTitle() + "Video.m4s";
        String audioUrl = path.getText() + "\\" + getVideoInformation.getTitle() + "Audio.m4s";
        String tvideoUrl = path.getText() + "\\" + getVideoInformation.getTitle() + "Video.mp4";
        String taudioUrl = path.getText() + "\\" + getVideoInformation.getTitle() + "Audio.mp3";
        String resultUrl = path.getText() + "\\" + getVideoInformation.getTitle() + ".mp4";
        download = new Download();
        Thread d = new Thread(()->{
            download.download(getVideoInformation.getVideoUrl(), videoUrl, getVideoInformation.getAv(), DownloadType.VIDEO);
            download.download(getVideoInformation.getAudioUrl(), audioUrl, getVideoInformation.getAv(), DownloadType.AUDIO);
        });
        d.start();
        p1PercentInformation.setText("正在下载");
        //开启线程更新进度条以及进行后续转码
        new Thread(()->{
            while (d.isAlive()){
                Platform.runLater(()->{
                    System.out.println("百分比" + download.getProcessPercent());
                    p1Progress.setProgress(download.getProcessPercent());
                    p1ProgressPercent.setText((int)(download.getProcessPercent()*100) + "%");
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(()->{
                p1ProgressPercent.setText("100%");
                p1PercentInformation.setText("正在转码,时间较长,请稍后");
            });
            TransCoding transCoding = new TransCoding(this.path.getText());
            transCoding.transM4s(videoUrl,tvideoUrl);
            transCoding.transM4s(audioUrl,taudioUrl);
            transCoding.merge(tvideoUrl,taudioUrl,resultUrl);
            Platform.runLater(()->{
                p1PercentInformation.setText("完成");
            });
            transCoding.cleanTmpFile();
        }).start();

    }

    @FXML
    private void buttonAboutOnClick(){
        new About().showWindow();
    }
}
