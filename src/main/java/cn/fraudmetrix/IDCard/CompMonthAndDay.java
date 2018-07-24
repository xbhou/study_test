/**
 * @author xiaobin.hou
 * @create 2018-07-02 16:36
 **/
package cn.fraudmetrix.IDCard;

import cn.fraudmetrix.util.DateUtils;
import cn.fraudmetrix.util.IdcardValidator;

import java.util.ArrayList;
import java.util.List;

public class CompMonthAndDay implements IdCardCompleteInf {

    public List<String> getCorrectIdCard(String missIdCard){

        List<String> resultList = new ArrayList<>();

        IdcardValidator validator = new IdcardValidator();

        String idCardEnd = missIdCard.substring(10);//身份证中的年
        String idCardStart = missIdCard.substring(0, 10);//身份证中月份之前的部分
        String idCardYear = missIdCard.substring(6,10);//身份证后四位

        StringBuffer idCardBf = new StringBuffer();

        int year = Integer.parseInt(idCardYear);

        for (int month = 1; month < 13; month++) {
            idCardBf.setLength(0);
            idCardBf.append(idCardStart);

            if (month < 10){
                idCardBf.append(0);
            }
            idCardBf.append(month);

            int maxDayOfMonth = DateUtils.getMaxDayOfMonth(year, month);
            StringBuffer idCardBakBf = new StringBuffer();
            for (int day = 1; day <= maxDayOfMonth; day++) {
                idCardBakBf.setLength(0);
                idCardBakBf.append(idCardBf);
                if (day < 10){
                    idCardBakBf.append(0);
                }
                idCardBakBf.append(day);
                idCardBakBf.append(idCardEnd);

                boolean validateResult = validator.isValidatedAllIdcard(idCardBakBf.toString());

                if (validateResult){
                    resultList.add(idCardBakBf.toString());
                }
            }
        }

        return resultList;
    }
}