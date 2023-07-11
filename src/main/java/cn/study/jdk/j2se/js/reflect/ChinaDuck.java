/**
 * @author xiaobin.hou
 * @create 2018-05-11 10:57
 **/
package cn.study.jdk.j2se.js.reflect;

public class ChinaDuck implements Duck  {

    private String color;

    public String name = "duck";

    protected String name2 = "deck";

    public ChinaDuck(){

    }

    public ChinaDuck(String color){
        this.color = color;
    }

    @Override
    public String getDuckColor() {
        return color;
    }

    @Override
    public void guaGua() {
        System.out.println("gua gua");
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}