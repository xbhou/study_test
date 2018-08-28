/**
 * @author xiaobin.hou
 * @create 2018-08-27 18:10
 **/
package cn.fraudmetrix.Integer;

import java.util.Properties;
import java.util.Set;

public class String2Int {

    public static void main(String[] args) {
        Properties properties = System.getProperties();

        Set<Object> objects = properties.keySet();
        for (Object key : objects){
            System.out.println(key + " : " + properties.get(key));
        }

        System.out.println("------------------------");

        System.out.println(Integer.getInteger("sun.arch.data.model"));
        System.out.println(Integer.getInteger("file.separator"));
        System.out.println(Integer.getInteger("sun.cpu.endian"));

        boolean aBoolean = Boolean.getBoolean("");

        Integer correct = Integer.valueOf("145");
        System.out.println(correct);

        try{
            Integer abc = Integer.valueOf("abc");
            System.out.println(abc);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        Integer.parseInt("123");


        System.out.println(Integer.valueOf(5) == Integer.valueOf(5));
        System.out.println(new Integer(5) == new Integer(5));
        System.out.println(Integer.valueOf(500) == Integer.valueOf(500));

    }
}