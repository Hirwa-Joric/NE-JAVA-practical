package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;

import java.util.List;

public interface PayrollService {
    
    List<PayslipResponseDTO> generatePayrollForMonth(int month, int year);
    
    List<PayslipResponseDTO> getPayslipsForEmployee(Long employeeId, int month, int year);
    
    List<PayslipResponseDTO> getAllPayslipsForMonthYear(int month, int year);
    
    List<PayslipResponseDTO> approveMonthlyPayroll(int month, int year);
}
