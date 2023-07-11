/**
 * @author xiaobin.hou
 * @create 2018-07-31 11:49
 **/
package cn.study.jdk.single;

/**
 * 使用内部类实现延迟加载
 */
public class Singleton5 {

    private Singleton5(){

    }

    public static class Holder{
        static Singleton5 instance = new Singleton5();
    }

    public static Singleton5 getInstance(){
        return Holder.instance;
    }
}