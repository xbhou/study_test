/**
 * @author xiaobin.hou
 * @create 2018-05-13 20:03
 **/
package cn.fraudmetrix.j2se.js.loadInit;

public class DealLoopTest {

    static class DealLoop{
        static{
            if (true){
                System.out.println(Thread.currentThread() + " init DealLoopClass");
                while(true){

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