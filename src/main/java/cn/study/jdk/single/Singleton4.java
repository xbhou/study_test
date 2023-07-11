/**
 * @author xiaobin.hou
 * @create 2018-07-31 11:39
 **/
package cn.study.jdk.single;

/**
 *  使用ThreadLocal避免double check的漏洞
 *  借助于ThreadLocal，将临界资源（需要同步的资源）线程局部化，
 *  具体到本例就是将双重检测的第一层检测条件 if (instance == null) 转换为了线程局部范围内来作。
 *  这里的ThreadLocal也只是用作标示而已，用来标示每个线程是否已访问过，如果访问过，则不再需要走同步块，
 *  这样就提高了一定的效率。但是ThreadLocal在1.4以前的版本都较慢，但这与volatile相比却是安全的。
 */
public class Singleton4 {

    private static final ThreadLocal perThreadInstance = new ThreadLocal();

    private static Singleton4 singleton = null;

    private Singleton4(){

    }

    public static Singleton4 getInstance(){
        if (perThreadInstance.get() == null){
            createInstance();
        }
        return  singleton;
    }
    //final作用？？？
    private static final void createInstance(){
        synchronized (Singleton4.class){
            if (singleton == null){
                singleton = new Singleton4();
            }
        }
        perThreadInstance.set(perThreadInstance);
    }
}