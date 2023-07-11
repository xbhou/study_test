/**
 * @author xiaobin.hou
 * @create 2018-07-31 10:33
 **/
package cn.study.jdk.single;

/**
 * 单例模式
 *  饿汉式
 */
public class Singleton1 {

    //将构造器私有化
    private Singleton1(){

    }

    private static final Singleton1 singleton = new Singleton1();

    public static Singleton1 getInstance(){
        return singleton;
    }

}