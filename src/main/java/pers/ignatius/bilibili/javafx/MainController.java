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
    private ThreadPoolExecutor pool;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    public void initialize(){
        choiceBox.setItems(FXCollections.observableArrayList("最清晰","1080P","720P","480P","360P"));
        choiceBox.setValue("最清晰");
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
        String t = choiceBox.getValue();
        if ("最清晰".equals(t))
            quality = VideoQuality.HIGHEST_QUALITY;
        else if ("1080P".equals(t))
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
                Platform.runLater(()->new Error(e.getMessage(), "网站无法访问,请检查您的网络").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (WebsiteNotEndWithAvException e) {
                Platform.runLater(()->new Error(e.getMessage(), "网址不符合规范").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (UnknownException e) {
                Platform.runLater(()->new Error(e.getMessage(), "未知异常").show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (AnalyzeUrlException e) {
                Platform.runLater(()->new Error(e.getMessage(), "URL解析异常,请联系作者").show());
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

    @FXML
    private CheckBox isSoftwareDecoding;
    @FXML
    private Spinner<Integer> threadNum;

    @FXML
    private void buttonStartOnClick(ActionEvent actionEvent){
        //没有选择路径
        if (path.getText() == null || "".equals(path.getText())){
            new Error("异常", "没有选择文件路径").show();
            return;
        }
        //创建线程池
        int threadNum = this.threadNum.getValue();
        System.out.println("使用线程数:" + threadNum);
        pool = new ThreadPoolExecutor(threadNum, threadNum, 120, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
        //计时
        long startTime = System.nanoTime();
        TransCoding.buildProgram(path.getText());
        //开始下载并转码视频
        List<VideoAllProcessing> videoAllProcessingList = new ArrayList<>();
        Scene scene = path.getScene();
        start.setDisable(true);
        for(int i=0;i<videoInformationList.size();i++){
            VideoAllProcessing videoAllProcessing = new VideoAllProcessing(videoInformationList.get(i), path.getText(), !isSoftwareDecoding.isSelected());
            ProgressBar progressBar = (ProgressBar) scene.lookup("#p1Progress" + i);
            Label ProgressPercent = (Label) scene.lookup("#p1ProgressPercent" + i);
            Label PercentInformation = (Label) scene.lookup("#p1PercentInformation" + i);
            videoAllProcessing.setProgressChangeAction(() -> {
                Platform.runLater(()->{
                    progressBar.setProgress(videoAllProcessing.getProgress().getProgress());
                    ProgressPercent.setText(videoAllProcessing.getProgress().toString());
                    PercentInformation.setText(videoAllProcessing.getMessage());
                });
            });
            videoAllProcessingList.add(videoAllProcessing);
            pool.execute(videoAllProcessing);
        }
        //启动线程更新UI
        new Thread(()->{
            pool.shutdown();
            //等待任务执行完毕
            while (!pool.isTerminated()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    new Error(e.getMessage(), "线程错误,请联系作者").show();
                    e.printStackTrace();
                }
            }
            TransCoding.disBuildProgram();
            System.out.println("共用时:"+ (System.nanoTime() - startTime)/1000000000.0);
        }).start();
        //保存此次下载目录
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
