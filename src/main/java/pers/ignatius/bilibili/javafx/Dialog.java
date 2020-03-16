package pers.ignatius.bilibili.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * @ClassName : Dialog
 * @Description : 提示框
 * @Author : IgnatiusGL
 * @Date : 2020-03-16 15:54
 */
public class Dialog {
    private String title;
    private String message;
    private DialogType dialogType;

    public Dialog(String title,String message,DialogType dialogType){
        this.title = title;
        this.message = message;
        this.dialogType = dialogType;
    }

    public void show(){
        Stage stage = new Stage();
        BorderPane root= new BorderPane();
        Label label = new Label(message);
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font("System Bold",15));
        BorderPane.setMargin(label, new Insets(50,50,30,20));
        root.setCenter(label);
        Button button = new Button("确定");
        BorderPane.setMargin(button, new Insets(0,0,30,0));
        button.setPrefHeight(30);
        button.setPrefWidth(70);
        BorderPane.setAlignment(button, Pos.CENTER);
        button.setOnAction((e)-> stage.close());
        root.setBottom(button);
        ImageView imageView = new ImageView();
        BorderPane.setMargin(imageView, new Insets(20,0,0,20));
        imageView.setFitHeight(91);
        imageView.setFitWidth(60);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        Image image = null;
        switch (dialogType){
            case ERROR:
                image = new Image(getClass().getResource("/img/error.png").toString());
                break;
            case WARING:
                image = new Image(getClass().getResource("/img/warning.png").toString());
                break;
            case SUCCESSFUL:
                image = new Image(getClass().getResource("/img/successful.png").toString());
                break;
        }
        imageView.setImage(image);
        root.setLeft(imageView);
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(getClass().getResource("/img/dialog.png").toString()));
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
}
