package rw.gov.erp.payroll.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.model.enums.PayslipStatus;
import rw.gov.erp.payroll.service.PayrollService;
import rw.gov.erp.payroll.util.CurrentUserUtil;

import java.util.List;

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {
    
    private final PayrollService payrollService;
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public ResponseEntity<List<PayslipResponseDTO>> getMyPayslips(
            @RequestParam int month,
            @RequestParam int year) {
        Long employeeId = CurrentUserUtil.getCurrentUserId();
        List<PayslipResponseDTO> payslips = payrollService.getPayslipsForEmployee(employeeId, month, year);
        return ResponseEntity.ok(payslips);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<PayslipResponseDTO>> getAllPayslips(
            @RequestParam int month,
            @RequestParam int year) {
        List<PayslipResponseDTO> payslips = payrollService.getAllPayslipsForMonthYear(month, year);
        return ResponseEntity.ok(payslips);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public ResponseEntity<List<PayslipResponseDTO>> getMyPendingPayslips(
            @RequestParam int month,
            @RequestParam int year) {
        Long employeeId = CurrentUserUtil.getCurrentUserId();
        List<PayslipResponseDTO> payslips = payrollService.getPayslipsForEmployee(employeeId, month, year);
        // Filter only PENDING payslips
        List<PayslipResponseDTO> pendingPayslips = payslips.stream()
                .filter(p -> p.getStatus() == PayslipStatus.PENDING)
                .toList();
        return ResponseEntity.ok(pendingPayslips);
    }
}
