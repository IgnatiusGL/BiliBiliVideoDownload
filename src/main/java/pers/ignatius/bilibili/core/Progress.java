package pers.ignatius.bilibili.core;

/**
 * @ClassName : Progress
 * @Description : 进度类
 * @Author : IgnatiusGL
 * @Date : 2020-03-02 16:33
 */
public class Progress {
    private double progress;

    public Progress() {
    }

    public Progress(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        if (progress>1){
            this.progress = 1;
        }else {
            this.progress = progress;
        }
    }

    @Override
    public String toString() {
        return (int)(progress*100) + "%";
    }
}
