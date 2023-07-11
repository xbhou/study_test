/**
 * @author xiaobin.hou
 * @create 2018-05-13 22:30
 **/
package cn.study.jdk.j2se.js.num;

public class IntegerTest {



    public static void main(String[] args) {

        String a[] ;
        Integer i = -129;
        Integer j = -129;

        System.out.println(i == j);


        int low = -128;
        int o = Integer.MAX_VALUE - (-low) -1;

        System.out.println(Integer.MAX_VALUE);
        System.out.println(o);
    }
}