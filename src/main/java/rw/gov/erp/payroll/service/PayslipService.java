package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;

import java.util.List;

public interface PayslipService {
    
    /**
     * Get all payslips for an employee by their username
     * @param username The username of the employee
     * @return List of payslips for the employee
     */
    List<PayslipResponseDTO> getEmployeePayslips(String username);
    
    /**
     * Get a specific payslip by ID for an employee
     * @param id The payslip ID
     * @param username The username of the employee requesting the payslip
     * @return The payslip if found and authorized
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authorized to view this payslip
     * @throws javax.persistence.EntityNotFoundException if the payslip does not exist
     */
    PayslipResponseDTO getPayslipForEmployeeById(Long id, String username);
}
