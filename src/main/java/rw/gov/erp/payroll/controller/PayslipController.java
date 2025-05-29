package rw.gov.erp.payroll.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.model.enums.PayslipStatus;
import rw.gov.erp.payroll.service.PdfService;
import rw.gov.erp.payroll.service.PayrollService;
import rw.gov.erp.payroll.service.PayslipService;
import rw.gov.erp.payroll.util.CurrentUserUtil;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayrollService payrollService;
    private final PayslipService payslipService;
    private final PdfService pdfService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<PayslipResponseDTO>> getMyPayslips(Principal principal) {
        List<PayslipResponseDTO> payslips = payslipService.getEmployeePayslips(principal.getName());
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
    
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long id, Principal principal) {
        // Get the payslip by ID and verify the user has access
        PayslipResponseDTO payslip = payslipService.getPayslipForEmployeeById(id, principal.getName());
        
        // Generate PDF
        byte[] pdfBytes = pdfService.generatePayslipPdf(payslip);
        
        // Create appropriate headers for PDF download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payslip-" + payslip.getMonth() + "-" + payslip.getYear() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
