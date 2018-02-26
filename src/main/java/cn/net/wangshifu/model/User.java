package cn.net.wangshifu.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String userPic;
    private int adminApply;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public int getAdminApply() {
        return adminApply;
    }

    public void setAdminApply(int adminApply) {
        this.adminApply = adminApply;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", userPic='" + userPic + '\'' +
                ", adminApply=" + adminApply +
                '}';
    }

}
