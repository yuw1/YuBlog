package cn.net.wangshifu.model;

import java.sql.Timestamp;

public class AdminApply {
    private int id;
    private int userId;
    private String userName;
    private Timestamp lastApplyTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getLastApplyTime() {
        return lastApplyTime;
    }

    public void setLastApplyTime(Timestamp lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    @Override
    public String toString() {
        return "AdminApply{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", lastApplyTime=" + lastApplyTime +
                '}';
    }
}
