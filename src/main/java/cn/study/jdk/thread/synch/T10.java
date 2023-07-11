/**
 * @author xiaobin.hou
 * @create 2018-08-02 14:30
 **/
package cn.study.jdk.thread.synch;

import java.util.concurrent.TimeUnit;

public class T10 {

    Object o = new Object();

    public void m() {
        synchronized (o) {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(System.currentTimeMillis() + Thread.currentThread().getName());
            }
        }
    }

    public static void main(String[] args) {
        T10 t = new T10();

        new Thread(() -> t.m(), "t1").start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t.o = new Object();

        new Thread(() -> t.m(), "t2").start();

        new Thread(() -> t.m(), "t3").start();
    }
}