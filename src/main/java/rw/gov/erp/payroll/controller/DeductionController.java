package rw.gov.erp.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.deduction.DeductionRequestDTO;
import rw.gov.erp.payroll.dto.deduction.DeductionResponseDTO;
import rw.gov.erp.payroll.service.DeductionService;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@RequiredArgsConstructor
public class DeductionController {
    
    private final DeductionService deductionService;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DeductionResponseDTO> createDeduction(@Valid @RequestBody DeductionRequestDTO requestDTO) {
        return new ResponseEntity<>(deductionService.createDeduction(requestDTO), HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<DeductionResponseDTO>> getAllDeductions() {
        return ResponseEntity.ok(deductionService.getAllDeductions());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeductionResponseDTO> getDeductionById(@PathVariable Long id) {
        return ResponseEntity.ok(deductionService.getDeductionById(id));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<DeductionResponseDTO> getDeductionByCode(@PathVariable String code) {
        return ResponseEntity.ok(deductionService.getDeductionByCode(code));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DeductionResponseDTO> updateDeduction(
            @PathVariable Long id, 
            @Valid @RequestBody DeductionRequestDTO requestDTO) {
        return ResponseEntity.ok(deductionService.updateDeduction(id, requestDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDeduction(@PathVariable Long id) {
        deductionService.deleteDeduction(id);
        return ResponseEntity.noContent().build();
    }
}
