/**
 * @author xiaobin.hou
 * @create 2018-07-24 19:29
 **/
package cn.fraudmetrix.thread.synch;

import org.junit.jupiter.api.Test;

public class TestSynchronize {

//    @Test
    public void test_no_synchronize(){
        final InsertData insertData = new InsertData();
        new Thread(){
            @Override
            public void run() {
                insertData.doInsert(Thread.currentThread());
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                insertData.doInsert(Thread.currentThread());
            }
        }.start();
    }

    @Test
    public void test_synchronize_method(){
        InsertDataSynMethod insertData = new InsertDataSynMethod();
        new Thread(){
            @Override
            public void run() {
                insertData.doInsert(Thread.currentThread());
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                insertData.doInsert(Thread.currentThread());
            }
        }.start();
    }

}