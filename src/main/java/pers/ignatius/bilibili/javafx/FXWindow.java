package pers.ignatius.bilibili.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pers.ignatius.bilibili.core.GetVideoInformation;
import pers.ignatius.bilibili.core.VideoQuality;

import java.io.IOException;
import java.net.URL;

/**
 * @ClassName : FXWindow
 * @Description : javafx程序的窗口
 * @Author : IgnatiusGL
 * @Date : 2020-02-28 21:48
 */
public class FXWindow extends Application {
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader;
        URL url = getClass().getResource("/fxml/main.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        BorderPane root;
        try {
            root = fxmlLoader.load(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
