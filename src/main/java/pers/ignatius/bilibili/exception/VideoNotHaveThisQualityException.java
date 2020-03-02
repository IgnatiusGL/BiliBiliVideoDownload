package pers.ignatius.bilibili.exception;

/**
 * @ClassName : VideoNotHaveThisQualityException
 * @Description : 没有此视频质量
 * @Author : IgnatiusGL
 * @Date : 2020-03-01 22:59
 */
public class VideoNotHaveThisQualityException extends Exception{
    public VideoNotHaveThisQualityException() {
        super("没有此视频质量");
    }
}
