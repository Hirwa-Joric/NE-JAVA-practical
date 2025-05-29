package rw.gov.erp.payroll.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import rw.gov.erp.payroll.model.Payslip;

import java.util.List;

@Getter
public class PayrollApprovedEvent extends ApplicationEvent {
    
    private final List<Payslip> approvedPayslips;
    
    public PayrollApprovedEvent(Object source, List<Payslip> approvedPayslips) {
        super(source);
        this.approvedPayslips = approvedPayslips;
    }
}
