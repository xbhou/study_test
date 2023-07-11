/**
 * @author xiaobin.hou
 * @create 2018-08-03 20:38
 **/
package cn.study.jdk.reflect;

public class ReflectTest {

    public static void main(String[] args) {
        try {
            Class<?> birdClass = Class.forName("cn.study.jdk.reflect.Bird");
            Object o = birdClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}