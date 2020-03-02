package pers.ignatius.bilibili.exception;

/**
 * @ClassName : InternetException
 * @Description : 网络错误
 * @Author : IgnatiusGL
 * @Date : 2020-03-01 22:16
 */
public class InternetException extends Exception{
    public InternetException() {
        super("网络错误");
    }
}
