/**
 * @author xiaobin.hou
 * @create 2018-07-24 19:34
 **/
package cn.study.jdk.thread.synch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InsertDataSynMethod {

    private List<Integer> dataList = new ArrayList<>();

    public synchronized void doInsert(Thread thread){
        for (int i = 0; i < 10; i++) {
            try {
                Random random = new Random();
                int i1 = random.nextInt(100) + 1;
                Thread.sleep(i1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(thread.getName() + " insert " + i);
            dataList.add(i);
        }
    }


}