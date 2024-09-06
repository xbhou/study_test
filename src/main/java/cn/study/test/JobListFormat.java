package cn.study.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class JobListFormat {

    //Frozen Active Deleted
    private final static List<String> JOB_STATUS = new ArrayList<>();

    private final static String TAB = "\\t";
    private final static String NEWLINE = "\\n";

    static {
        JOB_STATUS.add("Frozen");
        JOB_STATUS.add("Active");
        JOB_STATUS.add("Deleted");
    }

    public static void main(String[] args) throws IOException {
        // 使用ClassLoader读取资源文件
        InputStream inputStream = JobListFormat.class.getClassLoader().getResourceAsStream("jobList");
        FileWriter fos = new FileWriter("/Users/xiaobin/Desktop/output.txt");
        BufferedWriter writer = new BufferedWriter(fos);

        // 确保inputStream不为null
        if (inputStream == null) {
            System.out.println("资源文件未找到");
            return;
        }

        // 用于读取资源文件内容
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (Strings.isNullOrEmpty(line)) {
                    continue;
                }
                writer.write(line);
                if (JOB_STATUS.contains(line.trim())) {
                    writer.newLine();
                } else {
                    writer.write("\t");
                }
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
