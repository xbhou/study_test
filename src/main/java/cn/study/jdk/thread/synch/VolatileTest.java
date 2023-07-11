/**
 * @author xiaobin.hou
 * @create 2018-07-27 17:37
 **/
package cn.study.jdk.thread.synch;

public class VolatileTest {

    private  volatile int inc = 0;

    public void increase() {
        inc ++;
    }

    public static void main(String[] args) {

        System.out.println(Thread.currentThread());
        System.out.println(Thread.activeCount());

        final VolatileTest test = new VolatileTest();

        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        test.increase();
                    }
                }
            }.start();
        }
        int i = 0;
        while (Thread.activeCount() >2){
            System.out.println(i++);
            Thread.yield();
        }

        System.out.println(test.inc);
    }
}