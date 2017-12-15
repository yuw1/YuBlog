package cn.net.wangshifu.model;

public class User {
    private int id;
    private String nickname;
    private String password;
    private int token;

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nikename) {
        this.nickname = nikename;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nikename='" + nickname + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
