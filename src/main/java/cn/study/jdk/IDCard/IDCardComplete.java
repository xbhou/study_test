/**
 * @author xiaobin.hou
 * @create 2018-07-02 15:15
 **/
package cn.study.jdk.IDCard;

import cn.study.jdk.util.DateUtils;
import cn.study.jdk.util.IdcardValidator;

import java.util.Calendar;

public class IDCardComplete {

    public static void main(String[] args) {

        IdcardValidator validator = new IdcardValidator();

        String name = "付瑜";
        String idCard = "14272319880817";
        String correct = null;

        String idCardStart = idCard.substring(0, 10);
        String idCardEnd = idCard.substring(10);
        String idCardYear = idCard.substring(6,10);

        int year = Integer.parseInt(idCardYear);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year); // 设置年份

        StringBuffer idCardBf = new StringBuffer();


        for (int month = 1; month <= 12; month++) {
            idCardBf.setLength(0);
            idCardBf.append(idCardStart);

            StringBuffer monthBf = new StringBuffer();

            if (month < 10){
                monthBf.append("0");
            }
            monthBf.append(month);

            idCardBf.append(monthBf);
            int maxDay = DateUtils.getMaxDayOfMonth(year,month);

            StringBuffer dayBf = new StringBuffer();
            for (int day = 1; day <= maxDay; day++) {
                StringBuffer idCardBakBf = new StringBuffer(idCardBf);

                dayBf.setLength(0);
                if (day < 10){
                    dayBf.append("0");
                }
                dayBf.append(day);
                idCardBakBf.append(dayBf);
                idCardBakBf.append(idCardEnd);

                boolean result = validator.isValidatedAllIdcard(idCardBakBf.toString());

                if (result){
                    correct = idCardBakBf.toString();
                    break;
                }
            }
        }

        System.out.println(correct + "\t" + name);


//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.YEAR, 2010); // 2010年
//        c.set(Calendar.MONTH, 1); // 6 月
//        System.out.println("------------" + c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月的天数和周数-------------");
//        System.out.println("天数：" + c.getActualMaximum(Calendar.DAY_OF_MONTH));
//        System.out.println("周数：" + c.getActualMaximum(Calendar.WEEK_OF_MONTH));

    }





}