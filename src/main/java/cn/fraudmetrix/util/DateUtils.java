/**
 * @author xiaobin.hou
 * @create 2018-07-02 16:38
 **/
package cn.fraudmetrix.util;

import java.util.Calendar;

public class DateUtils {

    /**
     * 获取某月的最大天数
     * @param year
     * @param month
     * @return
     */
    public static int getMaxDayOfMonth(int year,int month){
        if (month <= 0 || month > 12){
            return 0;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year); // 设置年份
        c.set(Calendar.MONTH, month - 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}