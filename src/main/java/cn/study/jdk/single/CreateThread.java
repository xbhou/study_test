/**
 * @author xiaobin.hou
 * @create 2018-07-31 14:58
 **/
package cn.study.jdk.single;

import java.lang.reflect.Method;

public class CreateThread extends Thread{
    Object singleton;
    ClassLoader cl;

    public CreateThread(ClassLoader cl) {
        this.cl = cl;
    }

    public void run() {
        Class c;
        try {
            c = cl.loadClass("Singleton3");
            // 当两个不同命名空间内的类相互不可见时，可采用反射机制来访问对方实例的属性和方法
            Method m = c.getMethod("getInstance", new Class[] {});
            // 调用静态方法时，传递的第一个参数为class对象
            singleton = m.invoke(c, new Object[] {});
            c = null;
            cl = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}