package rw.gov.erp.payroll.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.NotificationLog;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.repository.NotificationLogRepository;
import rw.gov.erp.payroll.service.EmailService;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;
    private final Environment env;
    
    @Value("${app.email.from:noreply@erp.gov.rw}")
    private String fromEmail;
    
    @Value("${app.institution.name:Government of Rwanda}")
    private String institutionName;
    
    @Override
    @Async
    public void sendPayslipNotificationEmail(Employee employee, Payslip payslip) {
        String subject = String.format("Salary Credited - %s/%d", 
                Month.of(payslip.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH), 
                payslip.getYear());
        
        String message = String.format(
                "Dear %s, your salary of %s/%d from %s %s has been credited to your %s account successfully.",
                employee.getFirstName(),
                Month.of(payslip.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                payslip.getYear(),
                institutionName,
                payslip.getNetSalary(),
                employee.getCode()
        );
        
        // Create notification log entry
        NotificationLog notificationLog = NotificationLog.builder()
                .employee(employee)
                .recipientEmail(employee.getEmail())
                .subject(subject)
                .message(message)
                .status(NotificationLog.NotificationStatus.SENT) // Optimistic setting, will update if fails
                .build();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(employee.getEmail());
            helper.setSubject(subject);
            helper.setText(message, true); // true = isHtml
            
            mailSender.send(mimeMessage);
            log.info("Payslip notification email sent to {} for {}/{}", 
                    employee.getEmail(), payslip.getMonth(), payslip.getYear());
            
            // Save notification log as SENT
            notificationLogRepository.save(notificationLog);
            
        } catch (MailException | MessagingException e) {
            log.error("Failed to send payslip notification to {}: {}", employee.getEmail(), e.getMessage());
            
            // Update notification log to FAILED and save error message
            notificationLog.setStatus(NotificationLog.NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            notificationLogRepository.save(notificationLog);
        }
    }
}
