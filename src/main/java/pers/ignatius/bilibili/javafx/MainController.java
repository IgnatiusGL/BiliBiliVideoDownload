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

import static java.util.function.Predicate.not;

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
    private ChoiceBox<String> choiceBox;//选择视频清晰度栏

    /**
     * 初始化
     */
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
    private TextField videoUrl;//视频地址栏
    @FXML
    private Button start;//开始下载按钮
    @FXML
    private Button analyze;//开始解析按钮
    @FXML
    private VBox videoInformation;//视频信息栏


    /**
     * 解析视频地址按钮事件响应
     */
    @FXML
    protected void buttonAnalyseOnClick(ActionEvent actionEvent) {
        //清空之前的信息
        videoInformation.getChildren().clear();

        VideoQuality quality;
        if (choiceBox.getValue() == null){
            new Dialog("异常", "未选择视频质量",DialogType.ERROR).show();
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
                Platform.runLater(()->new Dialog(e.getMessage(), "网站无法访问,请检查您的网络",DialogType.ERROR).show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (WebsiteNotEndWithAvException e) {
                Platform.runLater(()->new Dialog(e.getMessage(), "网址不符合规范",DialogType.ERROR).show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (UnknownException e) {
                Platform.runLater(()->new Dialog(e.getMessage(), "未知异常",DialogType.ERROR).show());
                e.printStackTrace();
                Platform.runLater(()->{
                    analyze.setDisable(false);
                    analyze.setText("解析地址");
                });
                return;
            } catch (AnalyzeUrlException e) {
                Platform.runLater(()->new Dialog(e.getMessage(), "URL解析异常,请联系作者",DialogType.ERROR).show());
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

                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(true);
                checkBox.setId("isDownload" + i);
                checkBox.setAlignment(Pos.CENTER);
                checkBox.setPrefHeight(24);
                checkBox.setPrefWidth(70);
                hBox.getChildren().add(checkBox);
                isDownloadList.add(checkBox);

                Label label = new Label();
                label.setId("title" + i);
                label.setText(videoInformationList.get(i).getTitle());
                label.setAlignment(Pos.CENTER);
                label.setPrefHeight(24);
                label.setPrefWidth(231);
                hBox.getChildren().add(label);
                HBox.setMargin(label, new Insets(0,0,0,50));

                ProgressBar progressBar = new ProgressBar();
                progressBar.setId("progress" + i);
                progressBar.setPrefHeight(24);
                progressBar.setPrefWidth(400);
                progressBar.setProgress(0);
                HBox.setMargin(progressBar, new Insets(0,0,0,50));
                hBox.getChildren().add(progressBar);

                label = new Label();
                label.setId("progressPercent" + i);
                label.setPrefHeight(24);
                label.setPrefWidth(70);
                label.setText("0%");
                HBox.setMargin(label, new Insets(0,0,0,5));
                hBox.getChildren().add(label);

                label = new Label();
                label.setId("percentInformation" + i);
                label.setText("准备下载");
                label.setPrefHeight(24);
                label.setPrefWidth(181);
                HBox.setMargin(label, new Insets(0,0,0,10));
                hBox.getChildren().add(label);
                hBox.setMaxWidth(videoInformation.getWidth());

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
    private TextField path;//下载路径栏

    /**
     * 选择路径按钮事件响应
     */
    @FXML
    private void buttonSelectPathOnClick(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择视频保存路径");
        File selectedFile = directoryChooser.showDialog(new Stage());
        if(selectedFile == null)
            return;
        String path = selectedFile.getPath();
        this.path.setText(path);
    }

    @FXML
    private CheckBox isSoftwareDecoding;//是否使用软件解码栏
    @FXML
    private Spinner<Integer> threadNum;//线程数选择框
    @FXML
    private CheckBox isShutDownWhenFinished;//是否任务结束后关机选择框

    /**
     * 开始下载按钮事件响应
     */
    @FXML
    private void buttonStartOnClick(){
        //没有选择路径
        if (path.getText() == null || "".equals(path.getText())){
            new Dialog("异常", "没有选择文件路径",DialogType.ERROR).show();
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
        Scene scene = path.getScene();
        start.setDisable(true);
        new Dialog("警告", "下载期间占用系统资源较多,尽量减少电脑操作",DialogType.WARING).show();
        if (isShutDownWhenFinished.isSelected())
            new Dialog("警告", "您选择了在任务完成后关机",DialogType.WARING).show();
        for(int i=0;i<videoInformationList.size();i++){
            //判断是否要下载
            CheckBox checkBox = (CheckBox) scene.lookup("#isDownload" + i);
            if (!checkBox.isSelected()){
                continue;
            }

            VideoAllProcessing videoAllProcessing = new VideoAllProcessing(videoInformationList.get(i), path.getText(), !isSoftwareDecoding.isSelected());
            ProgressBar progressBar = (ProgressBar) scene.lookup("#progress" + i);
            Label ProgressPercent = (Label) scene.lookup("#progressPercent" + i);
            Label PercentInformation = (Label) scene.lookup("#percentInformation" + i);
            videoAllProcessing.setProgressChangeAction(() -> {
                Platform.runLater(()->{
                    progressBar.setProgress(videoAllProcessing.getProgress().getProgress());
                    ProgressPercent.setText(videoAllProcessing.getProgress().toString());
                    PercentInformation.setText(videoAllProcessing.getMessage());
                });
            });
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
                    Platform.runLater(()->
                        new Dialog(e.getMessage(), "线程错误,请联系作者",DialogType.ERROR).show()
                    );
                    e.printStackTrace();
                }
            }
            TransCoding.disBuildProgram();
            Platform.runLater(()->{
                new Dialog("完成", "任务完成用时:" + String.format("%.2f秒",(System.nanoTime() - startTime)/1000000000.0),DialogType.SUCCESSFUL).show();
                start.setDisable(false);
            });
            if (isShutDownWhenFinished.isSelected()){
                try {
                    Runtime.getRuntime().exec("cmd /c shutdown -s -t 0");
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(()->
                        new Dialog("异常", "关机错误",DialogType.ERROR).show()
                    );
                }
            }
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

    private List<CheckBox> isDownloadList = new ArrayList<>();//存放所有的选择框
    /**
     * 全选/全不选事件响应
     */
    @FXML
    private void buttonSelectAllOrCancelAllOnActon(){
        if (isDownloadList.stream().anyMatch(not(CheckBox::isSelected))){
            isDownloadList.forEach(e->e.setSelected(true));
        }else {
            isDownloadList.forEach(e->e.setSelected(false));
        }
    }

    /**
     * 关于按钮事件响应
     */
    @FXML
    private void buttonAboutOnClick(){
        new About().showWindow();
    }
}
