/**
 * @author xiaobin.hou
 * @create 2018-05-10 18:08
 **/
package cn.study.jdk.j2se.js.i18n;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatTest {

    public static void main(String[] args) {
        Date date = new Date();

        DateFormat defaultFormat = DateFormat.getDateInstance();
        System.out.println("default format " + defaultFormat.format(date));

        DateFormat defaultType = DateFormat.getDateInstance(DateFormat.FULL);
        System.out.println("default format type " + defaultType.format(date));



        System.out.println(DateFormat.getDateInstance(DateFormat.FULL,Locale.GERMAN).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.FULL,Locale.CHINA).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.AM_PM_FIELD,Locale.CHINA).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.DATE_FIELD,Locale.CHINA).format(date));
        System.out.println(DateFormat.getDateInstance(DateFormat.DAY_OF_WEEK_FIELD,Locale.CHINA).format(date));
    }

}