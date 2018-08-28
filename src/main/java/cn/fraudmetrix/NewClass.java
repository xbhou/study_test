/**
 * @author xiaobin.hou
 * @create 2018-08-02 18:08
 **/
package cn.fraudmetrix;

import java.lang.reflect.Constructor;

public class NewClass {

    {
        i = 3;
    }

    public int i = 2;

    public static int j = 2;

    public NewClass(){
        System.out.println(i);
    }

    public static void main(String[] args) throws Exception{
        NewClass newClass = new NewClass();
        System.out.println(newClass.i);

    }
}