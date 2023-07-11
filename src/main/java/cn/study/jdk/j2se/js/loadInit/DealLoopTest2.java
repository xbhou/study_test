/**
 * @author xiaobin.hou
 * @create 2018-05-13 20:03
 **/
package cn.study.jdk.j2se.js.loadInit;

import java.util.concurrent.TimeUnit;

public class DealLoopTest2 {

    static class DealLoop{
        static{
            if (true){
                System.out.println(Thread.currentThread() + " init DealLoopClass");

                try {
                    System.out.println(Thread.currentThread() + " thread start sleep");
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println(Thread.currentThread() + " thread end sleep");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Runnable script = new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread() + " start ");
                DealLoop dl = new DealLoop();
                System.out.println(Thread.currentThread() + " end");
            }
        };

        Thread thread1 = new Thread(script);
        Thread thread2 = new Thread(script);

        thread1.start();
        thread2.start();
    }
}