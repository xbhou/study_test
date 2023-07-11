/**
 * @author xiaobin.hou
 * @create 2018-07-22 18:18
 **/
package cn.study.jdk.threadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRejectTest {

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5));

        for(int i=0;i<30;i++){
            MyTask myTask = new MyTask(i);
            executor.execute(myTask);
            System.out.println("线程池中线程数目："+ executor.getPoolSize()
                    +"，队列中等待执行的任务数目："+ executor.getQueue().size()
                    +"，已执行完的任务数目："+executor.getCompletedTaskCount()
                    + "，最大线程数：" + executor.getLargestPoolSize());
        }
        executor.shutdown();

    }

    static class MyTask implements Runnable{

        private int taskNum;

        MyTask(int taskNum){
            this.taskNum = taskNum;
        }

        @Override
        public void run() {
            System.out.println("正在执行task "+taskNum);
            try {
                Thread.currentThread().sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("task "+taskNum+"执行完毕");
        }
    }
}