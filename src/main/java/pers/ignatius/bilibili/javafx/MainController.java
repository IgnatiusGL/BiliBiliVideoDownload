package pers.ignatius.bilibili.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pers.ignatius.bilibili.core.*;
import pers.ignatius.bilibili.exception.AnalyzeUrlException;
import pers.ignatius.bilibili.exception.InternetException;
import pers.ignatius.bilibili.exception.UnknownException;
import pers.ignatius.bilibili.exception.WebsiteNotEndWithAvException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : MainController
 * @Description : 主界面的控制
 * @Author : IgnatiusGL
 * @Date : 2020-02-28 22:39
 */
public class MainController {
    private GetVideoInformation getVideoInformation = new GetVideoInformation();
    private List<VideoInformation> videoInformationList;
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 120, TimeUnit.SECONDS,new LinkedBlockingQueue<>());

    @FXML
    private ChoiceBox choiceBox;

    @FXML
    public void initialize(){
        choiceBox.setItems(FXCollections.observableArrayList("1080P","720P","480P","360P"));
        Properties properties = new Properties();
        File file = new File("C:\\ProgramData\\bilibili.properties");
        try {
            if (!file.exists())
                file.createNewFile();
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = (String) properties.get("path");
        if (path != null && !"".equals(path)){
            this.path.setText(path);
        }
    }

    @FXML
    private TextField videoUrl;
    @FXML
    private Button start;
    @FXML
    private Button analyze;
    @FXML
    private VBox videoInformation;


    @FXML
    protected void buttonAnalyseOnClick(ActionEvent actionEvent) {
        //清空之前的信息
        videoInformation.getChildren().clear();

        VideoQuality quality;
        if (choiceBox.getValue() == null){
            new Error("异常", "未选择视频质量").show();
            return;
        }
        String t = choiceBox.getValue().toString();
        if ("1080P".equals(t))
            quality = VideoQuality.P1080;
        else if ("720P".equals(t))
            quality = VideoQuality.P720;
        else if ("480P".equals(t))
            quality = VideoQuality.P480;
        else
            quality = VideoQuality.P360;
        new Thread(()->{
            try {
                Platform.runLater(()->{
                    analyze.setDisable(true);
                    analyze.setText("正在解析");
                });
                videoInformationList = getVideoInformation.getVideoInformationFromUrl(videoUrl.getText(),quality);
            } catch (InternetException e) {
                Platform.runLater(()->new Error("网络异常", "网站无法访问,请检查您的网络").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (WebsiteNotEndWithAvException e) {
                Platform.runLater(()->new Error("网址异常", "网址不符合规范").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (UnknownException e) {
                Platform.runLater(()->new Error("未知异常", "未知异常").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (AnalyzeUrlException e) {
                Platform.runLater(()->new Error("解析异常", "URL解析异常,请联系作者").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            }
            //添加视频信息条
            for (int i=0;i<videoInformationList.size();i++){
                HBox hBox = new HBox();

                Label label = new Label();
                label.setId("p1Title" + i);
                label.setText(videoInformationList.get(i).getTitle());
                label.setAlignment(Pos.CENTER);
                label.setPrefHeight(24);
                label.setPrefWidth(231);
                hBox.getChildren().add(label);

                ProgressBar progressBar = new ProgressBar();
                progressBar.setId("p1Progress" + i);
                progressBar.setPrefHeight(24);
                progressBar.setPrefWidth(400);
                progressBar.setProgress(0);
                HBox.setMargin(progressBar, new Insets(0,0,0,50));
                hBox.getChildren().add(progressBar);

                label = new Label();
                label.setId("p1ProgressPercent" + i);
                label.setPrefHeight(24);
                label.setPrefWidth(70);
                label.setText("0%");
                HBox.setMargin(label, new Insets(0,0,0,5));
                hBox.getChildren().add(label);

                label = new Label();
                label.setId("p1PercentInformation" + i);
                label.setText("准备下载");
                label.setPrefHeight(24);
                label.setPrefWidth(181);
                HBox.setMargin(label, new Insets(0,0,0,50));
                hBox.getChildren().add(label);

                hBox.setPadding(new Insets(3,0,3,0));
                Platform.runLater(()->videoInformation.getChildren().add(hBox));
            }
            Platform.runLater(()->{
                analyze.setDisable(false);
                analyze.setText("解析地址");
                start.setDisable(false);
            });
        }).start();
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
        //没有选择路径
        if (path.getText() == null || "".equals(path.getText())){
            new Error("异常", "没有选择文件路径").show();
            return;
        }
        TransCoding.buildProgram(path.getText());
        //开始下载并转码视频
        List<VideoAllProcessing> videoAllProcessingList = new ArrayList<>();
        for(VideoInformation v:videoInformationList){
            VideoAllProcessing videoAllProcessing = new VideoAllProcessing(v, path.getText());
            videoAllProcessingList.add(videoAllProcessing);
            pool.execute(videoAllProcessing);
        }
        //启动线程更新UI
        Scene scene = path.getScene();
        new Thread(()->{
            pool.shutdown();
            for (int i=0;!pool.isTerminated();i++){
                if (i == videoAllProcessingList.size())
                    i = 0;
                ProgressBar progressBar = (ProgressBar) scene.lookup("#p1Progress" + i);
                Label ProgressPercent = (Label) scene.lookup("#p1ProgressPercent" + i);
                Label PercentInformation = (Label) scene.lookup("#p1PercentInformation" + i);
                VideoAllProcessing videoAllProcessing = videoAllProcessingList.get(i);
                Platform.runLater(()->{
                    progressBar.setProgress(videoAllProcessing.getProgress().getProgress());
                    ProgressPercent.setText(videoAllProcessing.getProgress().toString());
                    PercentInformation.setText(videoAllProcessing.getMessage());
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    new Error("网络异常", "网站无法访问,请检查您的网络").show();
                    e.printStackTrace();
                }
            }
            pool = new ThreadPoolExecutor(4, 4, 120, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
            start.setDisable(false);
            TransCoding.disBuildProgram();
        }).start();
        Properties properties = new Properties();
        properties.put("path", path.getText());
        try {
            properties.store(new FileOutputStream(new File("C:\\ProgramData\\bilibili.properties")), "保存上次路径");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void buttonAboutOnClick(){
        new About().showWindow();
    }
}
