package com.index_electric_server.reading.service.sendMail;


import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String content,
            boolean html,
            byte[] attachmentBytes,
            String attachmentFileName
    ) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, html);

            if (attachmentBytes != null && attachmentBytes.length > 0) {
                helper.addAttachment(attachmentFileName, new ByteArrayResource(attachmentBytes));
            }

            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Send email failed to: " + to, e);
        }
    }
}