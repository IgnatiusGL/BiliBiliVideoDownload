package pers.ignatius.test.core;

import javafx.scene.text.Text;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : ThreadTest
 * @Description : 多线程测试
 * @Author : IgnatiusGL
 * @Date : 2020-03-02 18:26
 */
public class ThreadTest {
    public static void main(String[] args) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
        for (int i=0;i<20;i++){
            pool.execute(new Test(i + ""));
        }
        pool.shutdown();
        while (!pool.isTerminated());
        System.out.println("结束");
    }

    public static class Test implements Runnable{
        String name;

        public Test(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("执行" + name);
            for (int i=0;i<100000;i++) System.out.println("hello " + name);
            System.out.println("结束" + name);
        }
    }
}
