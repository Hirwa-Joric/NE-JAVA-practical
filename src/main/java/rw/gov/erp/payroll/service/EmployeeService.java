package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.dto.employee.EmployeeRequestDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeResponseDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeUpdateDTO;

import java.util.List;

public interface EmployeeService {
    
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    
    EmployeeResponseDTO getEmployeeById(Long id);
    
    EmployeeResponseDTO getEmployeeByEmail(String email);
    
    List<EmployeeResponseDTO> getAllEmployees();
    
    EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto);
    
    EmployeeResponseDTO updateEmployeeSelf(String email, EmployeeUpdateDTO dto);
    
    void deleteEmployee(Long id);
}
