package cn.study.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CountryCodeClean {

    private final static Map<String, Integer> TOP_COUNTRY_CONFIG = new HashMap<>();

    static {
        TOP_COUNTRY_CONFIG.put("尼日利亚", 0);
        TOP_COUNTRY_CONFIG.put("肯尼亚", 1);
        TOP_COUNTRY_CONFIG.put("赞比亚", 2);
    }

    public static void main(String[] args) {
        // 使用ClassLoader读取资源文件
        InputStream inputStream = CountryCodeClean.class.getClassLoader().getResourceAsStream("countryCode.json");


        // 确保inputStream不为null
        if (inputStream == null) {
            System.out.println("资源文件未找到");
            return;
        }

        // 用于读取资源文件内容
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {

                JSONArray jsonArray = JSONArray.parseArray(line);
                Set<String> nameSet = TOP_COUNTRY_CONFIG.keySet();
                List<Map<String,Object>> configMapList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String geoCode = jsonObject.getString("geoCode");
                    String zhName = jsonObject.getString("zhName");
                    Map<String, Object> config = new HashMap<>();
                    config.put("code", geoCode);
                    config.put("name", zhName);

                    if (nameSet.contains(zhName)){
                        config.put("order", TOP_COUNTRY_CONFIG.get(zhName));
                    }
                    configMapList.add(config);
                }
                System.out.println(JSON.toJSONString(configMapList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
