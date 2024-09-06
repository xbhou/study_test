package cn.study.iso8601;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ZonedDateTimeTest {


    public static void main(String[] args) {
        testZoneDateTime();
    }
    /**
     * ISO 8601格式带时区信息
     * yyyy-MM-dd'T'hh:mm:ss[+|-]hh:mm
     */
    public static void testZoneDateTime() {
        // 格式化当前时间为ISO 8601格式带时区信息
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        String formattedDateTime = now.format(formatter);
        System.out.println("Formatted date time with zone: " + formattedDateTime);

        // 解析ISO 8601格式带时区信息的字符串为日期时间对象
        String isoZonedDateTimeString = "2023-03-10T07:49:57.601+00:00";
        ZonedDateTime parsedZonedDateTime = ZonedDateTime.parse(isoZonedDateTimeString, formatter);
        System.out.println("GMT时间: " + isoZonedDateTimeString);
        System.out.println("Parsed date time with zone: " + parsedZonedDateTime);

        Date GMTDate = Date.from(parsedZonedDateTime.toInstant());
        System.out.println("GMTDate: " + GMTDate);

        // 转换为东八区时间
        ZonedDateTime chinaTime = parsedZonedDateTime.withZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai"));
        System.out.println("中国标准时间：" + chinaTime);
        // 将ZonedDateTime转换为Date
        Date chinaDate = Date.from(chinaTime.toInstant());
        System.out.println("chinaDate: " + chinaDate);
    }


    public static void test(){
        // 格式化当前时间为ISO 8601格式
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        String formattedDateTime = now.format(formatter);
        System.out.println("Formatted date time: " + formattedDateTime);

        // 解析ISO 8601格式的字符串为日期时间对象
        String isoDateTimeString = "2023-03-10T07:49:57.601";
        LocalDateTime parsedDateTime = LocalDateTime.parse(isoDateTimeString, formatter);
        System.out.println("Parsed date time: " + parsedDateTime);
    }
}
