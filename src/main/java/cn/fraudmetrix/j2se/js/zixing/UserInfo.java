/**
 * @author xiaobin.hou
 * @create 2018-05-11 10:50
 **/
package cn.fraudmetrix.j2se.js.zixing;

public class UserInfo {

    private long userId;
    private String userName;
    private int age;
    private String emailAddress;


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}