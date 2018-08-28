/**
 * @author xiaobin.hou
 * @create 2018-07-31 15:00
 **/
package cn.fraudmetrix.single;

public class SingleTest {

    public static void main(String[] args) throws Exception{
        while (true) {
            // 不能让系统加载器直接或间接的成为父加载器
            MyClassLoader loader = new MyClassLoader(null);
            loader.setLoadPath("/Users/xiaobin.hou/IdeaProjects/study_test/target/classes/cn/fraudmetrix/single/");
            CreateThread ct1 = new CreateThread(loader);
            CreateThread ct2 = new CreateThread(loader);
            ct1.start();
            ct2.start();
            ct1.join();
            ct2.join();
            if (ct1.singleton != ct2.singleton) {
                System.out.println(ct1.singleton + " " + ct2.singleton);
            }
            // System.out.println(ct1.singleton + " " + ct2.singleton);
            ct1.singleton = null;
            ct2.singleton = null;
            Thread.yield();
        }
    }
}