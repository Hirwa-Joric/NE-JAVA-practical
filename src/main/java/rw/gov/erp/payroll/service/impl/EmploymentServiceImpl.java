package rw.gov.erp.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.employment.EmploymentRequestDTO;
import rw.gov.erp.payroll.dto.employment.EmploymentResponseDTO;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Employment;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.service.EmploymentService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmploymentServiceImpl implements EmploymentService {
    
    private final EmploymentRepository employmentRepository;
    private final EmployeeRepository employeeRepository;
    
    @Override
    @Transactional
    public EmploymentResponseDTO createEmployment(EmploymentRequestDTO dto) {
        // Find employee by id or code
        Employee employee;
        if (dto.getEmployeeId() != null) {
            employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getEmployeeId()));
        } else if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().isEmpty()) {
            employee = employeeRepository.findByCode(dto.getEmployeeCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "code", dto.getEmployeeCode()));
        } else {
            throw new IllegalArgumentException("Either employeeId or employeeCode must be provided");
        }
        
        // Generate unique code for employment
        String employmentCode = "EMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        
        // Create and save employment
        Employment employment = Employment.builder()
                .code(employmentCode)
                .employee(employee)
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .baseSalary(dto.getBaseSalary())
                .status(dto.getStatus() != null ? dto.getStatus() : EmploymentStatus.ACTIVE)
                .joinDate(dto.getJoinDate())
                .build();
        
        Employment savedEmployment = employmentRepository.save(employment);
        
        return mapToResponseDTO(savedEmployment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmploymentResponseDTO getEmploymentById(Long id) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));
        return mapToResponseDTO(employment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmploymentResponseDTO> getEmploymentsByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        return employmentRepository.findByEmployeeAndStatus(employee, EmploymentStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmploymentResponseDTO> getAllEmployments() {
        return employmentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public EmploymentResponseDTO updateEmployment(Long id, EmploymentRequestDTO dto) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));
        
        // Update fields
        if (dto.getDepartment() != null) {
            employment.setDepartment(dto.getDepartment());
        }
        if (dto.getPosition() != null) {
            employment.setPosition(dto.getPosition());
        }
        if (dto.getBaseSalary() != null) {
            employment.setBaseSalary(dto.getBaseSalary());
        }
        if (dto.getStatus() != null) {
            employment.setStatus(dto.getStatus());
        }
        if (dto.getJoinDate() != null) {
            employment.setJoinDate(dto.getJoinDate());
        }
        
        // Check if employee is being changed
        if ((dto.getEmployeeId() != null && !dto.getEmployeeId().equals(employment.getEmployee().getId())) ||
            (dto.getEmployeeCode() != null && !dto.getEmployeeCode().isEmpty() && 
             !dto.getEmployeeCode().equals(employment.getEmployee().getCode()))) {
            
            Employee employee;
            if (dto.getEmployeeId() != null) {
                employee = employeeRepository.findById(dto.getEmployeeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getEmployeeId()));
            } else {
                employee = employeeRepository.findByCode(dto.getEmployeeCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Employee", "code", dto.getEmployeeCode()));
            }
            employment.setEmployee(employee);
        }
        
        Employment updatedEmployment = employmentRepository.save(employment);
        return mapToResponseDTO(updatedEmployment);
    }
    
    @Override
    @Transactional
    public void deleteEmployment(Long id) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));
        
        // Soft delete - set status to INACTIVE
        employment.setStatus(EmploymentStatus.INACTIVE);
        employmentRepository.save(employment);
    }
    
    private EmploymentResponseDTO mapToResponseDTO(Employment employment) {
        Employee employee = employment.getEmployee();
        return EmploymentResponseDTO.builder()
                .id(employment.getId())
                .code(employment.getCode())
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .department(employment.getDepartment())
                .position(employment.getPosition())
                .baseSalary(employment.getBaseSalary())
                .status(employment.getStatus())
                .joinDate(employment.getJoinDate())
                .build();
    }
}
