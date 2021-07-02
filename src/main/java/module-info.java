module javafx {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.web;
    requires jdk.crypto.ec;

    exports pers.ignatius.bilibili.javafx;
    opens pers.ignatius.bilibili.javafx to javafx.controls,javafx.graphics,javafx.web,javafx.fxml,javafx.base;
}