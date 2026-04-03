package com.index_electric_server.reading.dto.sendMail;


public class MailSendResultDto {

    private Long companyId;
    private String companyName;
    private String email;
    private boolean success;
    private String message;

    public MailSendResultDto() {
    }

    public MailSendResultDto(Long companyId, String companyName, String email, boolean success, String message) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.email = email;
        this.success = success;
        this.message = message;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}