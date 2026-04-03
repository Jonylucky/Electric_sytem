package com.index_electric_server.reading.controller;



import com.index_electric_server.reading.dto.sendMail.MailSendResultDto;
import com.index_electric_server.reading.dto.sendMail.MailSendWithZipRequest;
import com.index_electric_server.reading.service.sendMail.CompanyContactMailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mail")
public class CompanyContactMailController {

    private final CompanyContactMailService companyContactMailService;

    public CompanyContactMailController(CompanyContactMailService companyContactMailService) {
        this.companyContactMailService = companyContactMailService;
    }

    @PostMapping("/send/company/{companyId}/zip")
    public ResponseEntity<List<MailSendResultDto>> sendMailWithZipByCompanyId(
            @PathVariable Long companyId,
            @RequestBody MailSendWithZipRequest request
    ) {
        List<MailSendResultDto> results = companyContactMailService.sendMailWithZipByCompanyId(
                companyId,
                request.getMonth(),
                request.getSubject(),
                request.getContent(),
                request.isHtml()
        );

        return ResponseEntity.ok(results);
    }
}