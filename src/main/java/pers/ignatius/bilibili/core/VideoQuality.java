package pers.ignatius.bilibili.core;

/**
 * @ClassName : VideoQuality
 * @Description : 视频的质量
 * @Author : IgnatiusGL
 * @Date : 2020-02-28 19:52
 */
public enum VideoQuality {
    HIGHEST_QUALITY,
    P1080(80),
    P720(64),
    P480(32),
    P360(16);

    private int code;

    VideoQuality(){

    }
    VideoQuality(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
