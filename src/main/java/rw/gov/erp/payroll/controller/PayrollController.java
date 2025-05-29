package rw.gov.erp.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.payslip.PayrollRequestDTO;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.service.PayrollService;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {
    
    private final PayrollService payrollService;
    
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<List<PayslipResponseDTO>> generatePayroll(@Valid @RequestBody PayrollRequestDTO requestDTO) {
        List<PayslipResponseDTO> payslips = payrollService.generatePayrollForMonth(requestDTO.getMonth(), requestDTO.getYear());
        return ResponseEntity.ok(payslips);
    }
    
    @PutMapping("/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<PayslipResponseDTO>> approvePayroll(@Valid @RequestBody PayrollRequestDTO requestDTO) {
        List<PayslipResponseDTO> approvedPayslips = payrollService.approveMonthlyPayroll(requestDTO.getMonth(), requestDTO.getYear());
        return ResponseEntity.ok(approvedPayslips);
    }
}
