package com.shariful.hello.notifications;

public class Data {

    private String user,body,tittle,sent;
    private Integer icon;

    public Data() {
    }

    public Data(String user, String body, String tittle, String sent, Integer icon) {
        this.user = user;
        this.body = body;
        this.tittle = tittle;
        this.sent = sent;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }




}
