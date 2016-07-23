package com.example.callnsmsnotify;

/**
 * Created by nika on 7/9/15.
 */
public class EmailRow {
    String subject = "", body = "";
    Boolean sent = false;

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    EmailRow(String subject, String body, Boolean sent) {
        this.subject = subject;
        this.body = body;
        this.sent = sent;


    }

}