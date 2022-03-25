package com.alight.android.aoa_launcher.utils;

/**
 * 九学王用户信息
 */
public class UserInfoBean {
    private  int id;
    private  String section;
    private  String grade;
    private byte[] userIamge;
    private  int  sex;
    private  int age;
    private  String nickname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public byte[] getUserIamge() {
        return userIamge;
    }

    public void setUserIamge(byte[] userIamge) {
        this.userIamge = userIamge;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
