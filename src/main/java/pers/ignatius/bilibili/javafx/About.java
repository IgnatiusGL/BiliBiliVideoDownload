package pers.ignatius.bilibili.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @ClassName : About
 * @Description : Start About Window
 * @Author : IgnatiusGL
 * @Date : 2020-02-29 14:28
 */
public class About {
    /**
     * 显示窗口
     */
    public void showWindow() {
        BorderPane root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/about.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("About");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
