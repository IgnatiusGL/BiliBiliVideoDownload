package pers.ignatius.bilibili.core;

/**
 * @ClassName : Progress
 * @Description : 进度类
 * @Author : IgnatiusGL
 * @Date : 2020-03-02 16:33
 */
public class Progress {
    private double progress;
    private ProgressChangeAction progressChangeAction;

    public Progress() {
    }

    public Progress(double progress) {
        this.progress = progress;
    }

    /**
     * 设置进度改变事件处理
     * @param progressChangeAction  处理类
     */
    public void setProgressChangeAction(ProgressChangeAction progressChangeAction){
        this.progressChangeAction = progressChangeAction;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        if (progress>1){
            this.progress = 1;
        }else {
            if (Double.compare(progress, this.progress) != 0){
                this.progress = progress;
                if (progressChangeAction != null)
                    progressChangeAction.changeAction();
            }
        }
    }

    @Override
    public String toString() {
        return (int)(progress*100) + "%";
    }
}
