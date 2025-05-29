package rw.gov.erp.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.employee.EmployeeRequestDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeResponseDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeUpdateDTO;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.service.EmployeeService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        // Check if email already exists
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        
        // Check if mobile already exists
        if (employeeRepository.existsByMobile(dto.getMobile())) {
            throw new IllegalArgumentException("Mobile number already in use: " + dto.getMobile());
        }
        
        // Generate unique code if not provided
        String employeeCode = dto.getCode();
        if (employeeCode == null || employeeCode.isEmpty()) {
            // Generate a code like EMP-xxxxxxxx (8 chars from UUID)
            String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            employeeCode = "EMP-" + randomPart;
        } else if (employeeRepository.existsByCode(employeeCode)) {
            throw new IllegalArgumentException("Employee code already in use: " + employeeCode);
        }
        
        // Create and save employee
        Employee employee = Employee.builder()
                .code(employeeCode)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(dto.getRoles())
                .mobile(dto.getMobile())
                .dateOfBirth(dto.getDateOfBirth())
                .status(dto.getStatus() != null ? dto.getStatus() : EmployeeStatus.ACTIVE)
                .build();
        
        Employee savedEmployee = employeeRepository.save(employee);
        
        return mapToResponseDTO(savedEmployee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponseDTO(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
        return mapToResponseDTO(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        // Update fields
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        
        // Check if mobile is being changed and if it's already in use by another employee
        if (!employee.getMobile().equals(dto.getMobile())) {
            if (employeeRepository.existsByMobile(dto.getMobile())) {
                throw new IllegalArgumentException("Mobile number already in use: " + dto.getMobile());
            }
            employee.setMobile(dto.getMobile());
        }
        
        employee.setDateOfBirth(dto.getDateOfBirth());
        
        // Only update status if it's provided
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
        
        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(updatedEmployee);
    }
    
    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployeeSelf(String email, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
        
        // Update fields
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        
        // Check if mobile is being changed and if it's already in use by another employee
        if (!employee.getMobile().equals(dto.getMobile())) {
            if (employeeRepository.existsByMobile(dto.getMobile())) {
                throw new IllegalArgumentException("Mobile number already in use: " + dto.getMobile());
            }
            employee.setMobile(dto.getMobile());
        }
        
        employee.setDateOfBirth(dto.getDateOfBirth());
        
        // Status cannot be changed by employee themselves, so we ignore dto.getStatus()
        
        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(updatedEmployee);
    }
    
    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        // Soft delete - set status to DISABLED
        employee.setStatus(EmployeeStatus.DISABLED);
        employeeRepository.save(employee);
    }
    
    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .code(employee.getCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .roles(employee.getRoles())
                .mobile(employee.getMobile())
                .dateOfBirth(employee.getDateOfBirth())
                .status(employee.getStatus())
                .build();
    }
}
