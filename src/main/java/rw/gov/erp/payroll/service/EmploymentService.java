package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.dto.employment.EmploymentRequestDTO;
import rw.gov.erp.payroll.dto.employment.EmploymentResponseDTO;

import java.util.List;

public interface EmploymentService {
    
    EmploymentResponseDTO createEmployment(EmploymentRequestDTO dto);
    
    EmploymentResponseDTO getEmploymentById(Long id);
    
    List<EmploymentResponseDTO> getEmploymentsByEmployeeId(Long employeeId);
    
    List<EmploymentResponseDTO> getAllEmployments();
    
    EmploymentResponseDTO updateEmployment(Long id, EmploymentRequestDTO dto);
    
    void deleteEmployment(Long id);
}
