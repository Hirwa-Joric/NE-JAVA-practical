package rw.gov.erp.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.employment.EmploymentRequestDTO;
import rw.gov.erp.payroll.dto.employment.EmploymentResponseDTO;
import rw.gov.erp.payroll.service.EmploymentService;

import java.util.List;

@RestController
@RequestMapping("/api/employments")
@RequiredArgsConstructor
public class EmploymentController {
    
    private final EmploymentService employmentService;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EmploymentResponseDTO> createEmployment(@Valid @RequestBody EmploymentRequestDTO requestDTO) {
        return new ResponseEntity<>(employmentService.createEmployment(requestDTO), HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<EmploymentResponseDTO>> getAllEmployments() {
        return ResponseEntity.ok(employmentService.getAllEmployments());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EmploymentResponseDTO> getEmploymentById(@PathVariable Long id) {
        return ResponseEntity.ok(employmentService.getEmploymentById(id));
    }
    
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<EmploymentResponseDTO>> getEmploymentsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employmentService.getEmploymentsByEmployeeId(employeeId));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EmploymentResponseDTO> updateEmployment(
            @PathVariable Long id, 
            @Valid @RequestBody EmploymentRequestDTO requestDTO) {
        return ResponseEntity.ok(employmentService.updateEmployment(id, requestDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<Void> deleteEmployment(@PathVariable Long id) {
        employmentService.deleteEmployment(id);
        return ResponseEntity.noContent().build();
    }
}
