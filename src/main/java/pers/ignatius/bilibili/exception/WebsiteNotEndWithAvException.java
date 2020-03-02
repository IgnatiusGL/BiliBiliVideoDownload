package pers.ignatius.bilibili.exception;

/**
 * @ClassName : WebsiteNotEndWithAvException
 * @Description : 网址不以av结尾异常
 * @Author : IgnatiusGL
 * @Date : 2020-03-01 22:07
 */
public class WebsiteNotEndWithAvException extends Exception {
    public WebsiteNotEndWithAvException() {
        super("网址不是以av+数字结尾");
    }
}
