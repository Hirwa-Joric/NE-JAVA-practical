package rw.gov.erp.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.employee.EmployeeRequestDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeResponseDTO;
import rw.gov.erp.payroll.dto.employee.EmployeeUpdateDTO;
import rw.gov.erp.payroll.service.EmployeeService;
import rw.gov.erp.payroll.util.CurrentUserUtil;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        return new ResponseEntity<>(employeeService.createEmployee(requestDTO), HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> getCurrentEmployee() {
        String email = CurrentUserUtil.getCurrentUserEmail();
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(email));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id, 
            @Valid @RequestBody EmployeeUpdateDTO updateDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, updateDTO));
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public ResponseEntity<EmployeeResponseDTO> updateCurrentEmployee(
            @Valid @RequestBody EmployeeUpdateDTO updateDTO) {
        String email = CurrentUserUtil.getCurrentUserEmail();
        return ResponseEntity.ok(employeeService.updateEmployeeSelf(email, updateDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
