package pers.ignatius.bilibili.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * @ClassName : AboutController
 * @Description : About窗口的控制
 * @Author : IgnatiusGL
 * @Date : 2020-02-29 14:36
 */
public class AboutController {
    @FXML
    private Button buttonOK;
    @FXML
    public void buttonOKOnClick(){
        Stage stage = (Stage) buttonOK.getScene().getWindow();
        stage.close();
    }
}
