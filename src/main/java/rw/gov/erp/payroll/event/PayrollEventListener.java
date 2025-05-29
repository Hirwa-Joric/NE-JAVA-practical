package rw.gov.erp.payroll.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollEventListener {
    
    private final EmailService emailService;
    
    @EventListener
    public void handlePayrollApprovedEvent(PayrollApprovedEvent event) {
        log.info("Handling payroll approved event with {} payslips", event.getApprovedPayslips().size());
        
        for (Payslip payslip : event.getApprovedPayslips()) {
            Employee employee = payslip.getEmployment().getEmployee();
            emailService.sendPayslipNotificationEmail(employee, payslip);
        }
    }
}
