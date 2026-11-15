package cn.net.wangshifu.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminApply implements Serializable {
    private int id;
    private int userId;
    private String userName;
    private LocalDateTime lastApplyTime;

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

    public LocalDateTime getLastApplyTime() {
        return lastApplyTime;
    }

    public void setLastApplyTime(LocalDateTime lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    /** Rendered by the templates; avoids printing {@code LocalDateTime.toString()}. */
    public String getLastApplyTimeText() {
        return lastApplyTime == null ? "" : lastApplyTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
