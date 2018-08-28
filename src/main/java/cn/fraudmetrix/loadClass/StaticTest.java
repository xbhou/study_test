/**
 * @author xiaobin.hou
 * @create 2018-08-02 17:44
 **/
package cn.fraudmetrix.loadClass;

public class StaticTest {

    public static void main(String[] args) {
        staticFunction();
    }

    static StaticTest st = new StaticTest();

    static {
        System.out.println("1");
    }

    {
        System.out.println("2");
    }

    StaticTest() {
        System.out.println("3");
        System.out.println("a=" + a + ",b=" + b);
    }

    public static void staticFunction() {
        System.out.println("4");
    }

    int a = 110;
    static int b = 112;
}

//含有main方法的类，会被初始化，类构造器会被调用
//调用顺序
//static StaticTest st = new StaticTest();
    //此时b = 0
//static {System.out.println("1");}
//public static void staticFunction() {System.out.println("4");}
//static int b = 112;
