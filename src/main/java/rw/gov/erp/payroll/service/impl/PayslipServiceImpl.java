package rw.gov.erp.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Employment;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.repository.PayslipRepository;
import rw.gov.erp.payroll.service.PayslipService;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;

    @Override
    public List<PayslipResponseDTO> getEmployeePayslips(String username) {
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("Employee not found with email: " + username));
        
        // Find all active employments for this employee
        List<Employment> employments = employmentRepository.findByEmployeeAndStatus(
            employee, rw.gov.erp.payroll.model.enums.EmploymentStatus.ACTIVE
        );
        
        // Find all payslips for these employments
        List<Payslip> payslips = payslipRepository.findAll().stream()
                .filter(p -> employments.stream()
                        .anyMatch(e -> e.getId().equals(p.getEmployment().getId())))
                .collect(Collectors.toList());
        
        return payslips.stream()
                .map(this::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PayslipResponseDTO getPayslipForEmployeeById(Long id, String username) {
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("Employee not found with email: " + username));
        
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Payslip not found with id: " + id));
        
        // Get the employee's active employments
        List<Employment> employments = employmentRepository.findByEmployeeAndStatus(
            employee, rw.gov.erp.payroll.model.enums.EmploymentStatus.ACTIVE
        );
        
        // Check if the payslip belongs to one of the employee's employments
        boolean isAuthorized = employments.stream()
                .anyMatch(e -> e.getId().equals(payslip.getEmployment().getId()));
        
        // If not directly authorized, check if user has admin or manager role
        if (!isAuthorized) {
            Collection<? extends GrantedAuthority> authorities = 
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            
            isAuthorized = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                             auth.getAuthority().equals("ROLE_MANAGER"));
        }
        
        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to view this payslip");
        }
        
        return mapToPayslipResponseDTO(payslip);
    }
    
    private PayslipResponseDTO mapToPayslipResponseDTO(Payslip payslip) {
        // Get employee from the employment relationship
        Employee employee = payslip.getEmployment().getEmployee();
                
        return PayslipResponseDTO.builder()
                .id(payslip.getId())
                .employmentId(payslip.getEmployment().getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeCode(payslip.getEmployment().getCode()) // Using employment code as employee code
                .month(payslip.getMonth())
                .year(payslip.getYear())
                .grossSalary(payslip.getGrossSalary())
                .netSalary(payslip.getNetSalary())
                .employeeTaxedAmount(payslip.getEmployeeTaxedAmount())
                .otherTaxedAmount(payslip.getOtherTaxedAmount())
                .pensionAmount(payslip.getPensionAmount())
                .medicalInsuranceAmount(payslip.getMedicalInsuranceAmount())
                .houseAmount(payslip.getHouseAmount())
                .transportAmount(payslip.getTransportAmount())
                .status(payslip.getStatus())
                .build();
    }
}
