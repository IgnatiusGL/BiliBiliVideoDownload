package pers.ignatius.bilibili.exception;

/**
 * @ClassName : FileUnexpectedEndException
 * @Description : 出乎意料的文件结尾异常
 * @Author : IgnatiusGL
 * @Date : 2020-03-12 10:51
 */
public class FileUnexpectedEndException extends Exception{
    public FileUnexpectedEndException(String message) {
        super("意外的文件结尾:" + message);
    }
}
