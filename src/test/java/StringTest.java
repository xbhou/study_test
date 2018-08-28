/**
 * @author xiaobin.hou
 * @create 2018-08-14 12:31
 **/

public class StringTest {

    public static String separator = ".";
    public static int INDEX_NOT_FOUND = -1;

    public static void main(String[] args) {
        String test = "QueryEntRelatedRisk.ent_related_risk_message.ent_biz_message.manager_info[0].smb_web_index";
        String str = "a.b.c.d.e.f";



        if (test.contains("[") && test.contains("]")){
            String substring = test.substring(test.indexOf("["), test.indexOf("]") +1);
            System.out.println(substring);

            System.out.println(getStr(substring,str));
        }


    }

    public static String getStr(String add,String str){
        int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }

        String before = str.substring(0, pos);
        StringBuffer addBF = new StringBuffer();

        addBF.append(before).append(add).append(str.substring(pos));
        return addBF.toString();
    }
}