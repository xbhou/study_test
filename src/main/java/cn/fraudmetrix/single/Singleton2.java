/**
 * @author xiaobin.hou
 * @create 2018-07-31 10:36
 **/
package cn.fraudmetrix.single;

/**
 * 懒汉式
 *  同步延迟加载
 */
public class Singleton2 {

    private Singleton2(){

    }

    private static Singleton2 singleton = null;

    /**
     * 同步锁synchronized对getInstance方法加锁，所有线程获取
     * 实例都会被锁限制。效率低
     * @return
     */
    public synchronized Singleton2 getInstance(){
        if (singleton == null){
            singleton = new Singleton2();
        }

        return singleton;
    }
}