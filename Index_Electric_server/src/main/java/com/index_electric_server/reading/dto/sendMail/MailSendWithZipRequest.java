package com.index_electric_server.reading.dto.sendMail;


public class MailSendWithZipRequest {

    private String month;
    private String subject;
    private String content;
    private boolean html;

    public MailSendWithZipRequest() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }
}