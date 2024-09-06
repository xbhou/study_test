package cn.study.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class ReadPropertiesFile {

    private static final String BASE_DIR = "/Users/xiaobin/Desktop/";
    public static void main(String[] args) {

        Properties usWebProperties = loadProperties(BASE_DIR + "us-authwebapp");
        Properties cnWebProperties = loadProperties(BASE_DIR + "cn-authwebapp.properties");

        Set<Object> usWebKeySet = usWebProperties.keySet();
        Set<Object> cnWebKeySet = cnWebProperties.keySet();

        for (Object usWebKey : usWebKeySet){
            if (!cnWebKeySet.contains(usWebKey)){
                System.out.println("us-web-ext " + usWebKey);
            }
        }

        for (Object cnWebKey : cnWebKeySet){
            if (!usWebKeySet.contains(cnWebKey)){
                System.out.println("cn-web-ext " + cnWebKey);
            }
        }


        Properties usServApplication = loadProperties(BASE_DIR + "us-authserv-application");
        Properties cnServApplication = loadProperties(BASE_DIR + "cn-authserv-application.properties");

        Set<Object> usApplicationSet = usServApplication.keySet();
        Set<Object> cnApplicationSet = cnServApplication.keySet();


        for (Object usApplicationKey : usApplicationSet){
            if (!cnApplicationSet.contains(usApplicationKey)){
                System.out.println("us-serv-application-ext " + usApplicationKey);
            }
        }

        for (Object cnApplicationKey : cnApplicationSet){
            if (!usApplicationSet.contains(cnApplicationKey)){
                System.out.println("cn-serv-application-ext " + cnApplicationKey);
            }
        }
    }


    private static Properties loadProperties(String filePath) {

        FileInputStream fileInputStream = null;
        try {
            Properties properties = new Properties();
            fileInputStream = new FileInputStream(filePath);
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (Objects.nonNull(fileInputStream)){
                try {
                    fileInputStream.close();
                }catch (Exception e){
                    // ignore
                }
            }
        }
        return null;
    }
}