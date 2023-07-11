/**
 * @author xiaobin.hou
 * @create 2018-07-31 11:04
 **/
package cn.study.jdk.single;

/**
 * 单例模式
 *  懒汉式，dubbo check
 *  为处理原版非延迟加载方式瓶颈问题，我们需要避免过多的同步，只需要在第一次创建实例时才同步
 *  一旦创建成功，以后获取实例时就不需要获取同步锁了。
 *  我们可以使用同步代码块，对实例化的代码块进行同步，同步代码块里需要进行二次检查
 *
 *  在JDK1.5之前double check是行不通的。因为由于Java内存模型允许所谓的"无序写入"，从而导致同步块外面的
 *  if(instance == null)可能看到已存在，但是不完整的实例。
 *      无序写入：
 *          下面代码的//3处，实例化一个Singleton3对象，并初始化变量instance引用Singleton3对象
 *          这行代码的问题是：在 Singleton 构造函数体执行之前，变量 instance 可能成为非 null 的，
 *          即赋值语句在对象实例化之前调用，此时别的线程得到的是一个还会初始化的对象，这样会导致系统崩溃
 *      1、线程 1 进入 getInstance() 方法。
 *      2、由于 instance 为 null，线程 1 在 //1 处进入 synchronized 块。
 *      3、线程 1 前进到 //3 处，但在构造函数执行之前，变量instance被赋值未实例完成的对象的引用，此时instance成为非 null。
 *      4、线程 1 被线程 2 预占。
 *      5、线程 2 检查实例是否为 null。因为实例不为 null，
 *          线程 2 将 instance 引用返回给一个构造完整但部分初始化了的 Singleton 对象。
 *      6、线程 2 被线程 1 预占。
 *      7、线程 1 通过运行 Singleton 对象的构造函数并将引用返回给它，来完成对该对象的初始化。
 *  JDK5.0以后版本若instance为volatile则可行
 *
 *
 *
 */
public class Singleton3 {

    private Singleton3(){

    }

    private static volatile Singleton3 instance = null;

    /**
     *
     * @return
     */
    public static Singleton3 getInstance(){
        if (instance == null){
            synchronized (Singleton3.class){        //1
                if (instance == null){             //2
                    instance = new Singleton3();   //3
                }
            }
        }
        return instance;
    }

}