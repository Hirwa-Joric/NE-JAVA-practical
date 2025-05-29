package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Payslip;

public interface EmailService {
    
    void sendPayslipNotificationEmail(Employee employee, Payslip payslip);
    
}
