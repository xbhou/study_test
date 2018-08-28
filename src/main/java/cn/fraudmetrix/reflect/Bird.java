/**
 * @author xiaobin.hou
 * @create 2018-08-03 20:36
 **/
package cn.fraudmetrix.reflect;

public class Bird {

    private boolean isFlay;
    private String color;

    public boolean isFlay() {
        return isFlay;
    }

    public void setFlay(boolean flay) {
        isFlay = flay;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}