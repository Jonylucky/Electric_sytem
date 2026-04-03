package com.index_electric_server.reading.service.sendMail;

import com.index_electric_server.reading.dto.sendMail.MailSendResultDto;
import com.index_electric_server.reading.entity.Company;
import com.index_electric_server.reading.entity.CompanyContact;
import com.index_electric_server.reading.repository.CompanyContactRepository;
import com.index_electric_server.reading.repository.CompanyRepository;
import com.index_electric_server.reading.service.export.ZipExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyContactMailService {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final MailService mailService;
    private final ZipExportService zipExportService;

    public CompanyContactMailService(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            MailService mailService,
            ZipExportService zipExportService
    ) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.mailService = mailService;
        this.zipExportService = zipExportService;
    }

    @Transactional(readOnly = true)
    public List<MailSendResultDto> sendMailWithZipByCompanyId(
            Long companyId,
            String month,
            String subject,
            String content,
            boolean html
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        List<CompanyContact> contacts = companyContactRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId);

        if (contacts == null || contacts.isEmpty()) {
            throw new RuntimeException("No active contact found for companyId: " + companyId);
        }

        byte[] zipBytes;
        try {
            zipBytes = zipExportService.exportZipByMonthAndCompanyId(month, companyId);
        } catch (Exception e) {
            throw new RuntimeException("Export zip failed for companyId: " + companyId + ", month: " + month, e);
        }

        String zipFileName = "bao_cao_chi_so_dien_" + companyId + "_" + month + ".zip";

        List<MailSendResultDto> results = new ArrayList<>();

        for (CompanyContact contact : contacts) {
            try {
                String finalContent = buildContent(company.getCompanyName(), content, html);

                mailService.sendEmailWithAttachment(
                        contact.getEmail(),
                        subject,
                        finalContent,
                        html,
                        zipBytes,
                        zipFileName
                );

                results.add(new MailSendResultDto(
                        company.getCompanyId(),
                        company.getCompanyName(),
                        contact.getEmail(),
                        true,
                        "Sent successfully"
                ));
            } catch (Exception e) {
                results.add(new MailSendResultDto(
                        company.getCompanyId(),
                        company.getCompanyName(),
                        contact.getEmail(),
                        false,
                        e.getMessage()
                ));
            }
        }

        return results;
    }

    private String buildContent(String companyName, String rawContent, boolean html) {
        if (html) {
            // Thay đổi cấu trúc HTML để chuyên nghiệp hơn (có khung, màu sắc tinh tế)
            return """
                <html>
                  <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 20px auto; padding: 25px; border: 1px solid #e0e0e0; border-radius: 8px;">
                      <p style="margin-top: 0;">Kính gửi Quý đối tác <b>%s</b>,</p>
                      
                      <div style="margin: 20px 0; white-space: pre-line;">
                        %s
                      </div>
                      
                      <p style="margin-bottom: 5px;">Trân trọng,</p>
                      <p style="font-weight: bold; color: #0056b3; margin-top: 0;">Đội ngũ CSKH</p>
                      
                      <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                      <p style="font-size: 12px; color: #888;">Đây là email tự động, vui lòng không phản hồi trực tiếp vào email này.</p>
                    </div>
                  </body>
                </html>
                """.formatted(companyName, rawContent);
        }

        // Bản văn bản thuần (Plain Text) cũng cần chỉn chu
        return """
            Kính gửi Quý đối tác %s,

            %s

            Trân trọng,
            Đội ngũ CSKH
            """.formatted(companyName, rawContent);
    }
}