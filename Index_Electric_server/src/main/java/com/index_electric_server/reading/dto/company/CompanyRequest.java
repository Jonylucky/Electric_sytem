package com.index_electric_server.reading.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyRequest {

    @NotBlank(message = "companyName không được để trống")
    @Size(max = 255, message = "companyName tối đa 255 ký tự")
    private String companyName;

    public CompanyRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}