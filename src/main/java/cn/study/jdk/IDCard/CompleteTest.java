/**
 * @author xiaobin.hou
 * @create 2018-07-02 15:15
 **/
package cn.study.jdk.IDCard;

import java.util.List;

public class CompleteTest {

    public static void main(String[] args) {

        IdCardCompleteInf idCardComplete = new CompMonthAndDay();

        String name = "付瑜";
        String missIdCard = "61011119881520";

        List<String> resultList = idCardComplete.getCorrectIdCard(missIdCard);

        if (resultList != null){
            System.out.println("符合身份证规则的个数为：" + resultList.size());
            for (String idCard : resultList){
                System.out.println(idCard);
            }
        }else{
            System.out.println("补全失败");
        }


//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.YEAR, 2010); // 2010年
//        c.set(Calendar.MONTH, 1); // 6 月
//        System.out.println("------------" + c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月的天数和周数-------------");
//        System.out.println("天数：" + c.getActualMaximum(Calendar.DAY_OF_MONTH));
//        System.out.println("周数：" + c.getActualMaximum(Calendar.WEEK_OF_MONTH));

    }





}