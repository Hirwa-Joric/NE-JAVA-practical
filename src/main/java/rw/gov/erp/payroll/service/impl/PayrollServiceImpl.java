package rw.gov.erp.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.event.PayrollApprovedEvent;
import rw.gov.erp.payroll.model.Deduction;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Employment;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;
import rw.gov.erp.payroll.model.enums.PayslipStatus;
import rw.gov.erp.payroll.repository.DeductionRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.repository.PayslipRepository;
import rw.gov.erp.payroll.service.PayrollService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {
    
    private final EmploymentRepository employmentRepository;
    private final DeductionRepository deductionRepository;
    private final PayslipRepository payslipRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public List<PayslipResponseDTO> generatePayrollForMonth(int month, int year) {
        // Get all deduction rates
        Map<String, BigDecimal> deductionRates = fetchDeductionRates();
        
        // Get all active employments
        List<Employment> activeEmployments = employmentRepository.findByStatus(EmploymentStatus.ACTIVE);
        
        // Process each active employment
        List<Payslip> generatedPayslips = activeEmployments.stream()
                .map(employment -> {
                    // Check if payslip exists for this employment/month/year
                    Optional<Payslip> existingPayslip = payslipRepository.findByEmploymentIdAndMonthAndYear(
                            employment.getId(), month, year);
                    
                    // If exists and PAID, skip. If PENDING, delete and recreate
                    if (existingPayslip.isPresent()) {
                        if (existingPayslip.get().getStatus() == PayslipStatus.PAID) {
                            log.info("Payslip already PAID for employmentId: {}, month: {}, year: {}. Skipping.",
                                    employment.getId(), month, year);
                            return existingPayslip.get();
                        } else {
                            log.info("Deleting existing PENDING payslip for employmentId: {}, month: {}, year: {}",
                                    employment.getId(), month, year);
                            payslipRepository.delete(existingPayslip.get());
                        }
                    }
                    
                    // Calculate and store new payslip
                    return calculateAndStorePayslip(employment, month, year, deductionRates);
                })
                .collect(Collectors.toList());
        
        // Map to DTOs and return
        return generatedPayslips.stream()
                .map(this::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponseDTO> getPayslipsForEmployee(Long employeeId, int month, int year) {
        List<Payslip> payslips = payslipRepository.findByEmploymentEmployeeIdAndMonthAndYear(employeeId, month, year);
        return payslips.stream()
                .map(this::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponseDTO> getAllPayslipsForMonthYear(int month, int year) {
        List<Payslip> payslips = payslipRepository.findByMonthAndYear(month, year);
        return payslips.stream()
                .map(this::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public List<PayslipResponseDTO> approveMonthlyPayroll(int month, int year) {
        // Find all PENDING payslips for the month/year
        List<Payslip> pendingPayslips = payslipRepository.findByMonthAndYearAndStatus(month, year, PayslipStatus.PENDING);
        
        if (pendingPayslips.isEmpty()) {
            log.info("No PENDING payslips found for month: {}, year: {}", month, year);
            return List.of();
        }
        
        // Update status to PAID and set payment date
        LocalDateTime now = LocalDateTime.now();
        List<Payslip> approvedPayslips = pendingPayslips.stream()
                .map(payslip -> {
                    payslip.setStatus(PayslipStatus.PAID);
                    payslip.setPaymentDate(now);
                    return payslipRepository.save(payslip);
                })
                .collect(Collectors.toList());
        
        log.info("Approved {} payslips for month: {}, year: {}", approvedPayslips.size(), month, year);
        
        // Publish event for email notifications
        eventPublisher.publishEvent(new PayrollApprovedEvent(this, approvedPayslips));
        
        // Map to DTOs and return
        return approvedPayslips.stream()
                .map(this::mapToPayslipResponseDTO)
                .collect(Collectors.toList());
    }
    
    private Map<String, BigDecimal> fetchDeductionRates() {
        List<Deduction> deductions = deductionRepository.findAll();
        Map<String, BigDecimal> rateMap = new HashMap<>();
        
        for (Deduction deduction : deductions) {
            rateMap.put(deduction.getCode(), deduction.getPercentage());
        }
        
        return rateMap;
    }
    
    private Payslip calculateAndStorePayslip(Employment employment, int month, int year, Map<String, BigDecimal> deductionRates) {
        // Get base salary from employment
        BigDecimal baseSalary = employment.getBaseSalary();
        
        // Calculate housing amount (14%)
        BigDecimal housingRate = deductionRates.getOrDefault("HOUSING", new BigDecimal("0.14"));
        BigDecimal housingAmount = baseSalary.multiply(housingRate).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate transport amount (14%)
        BigDecimal transportRate = deductionRates.getOrDefault("TRANSPORT", new BigDecimal("0.14"));
        BigDecimal transportAmount = baseSalary.multiply(transportRate).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate gross salary
        BigDecimal grossSalary = baseSalary.add(housingAmount).add(transportAmount).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate deductions from base salary
        BigDecimal employeeTaxRate = deductionRates.getOrDefault("EMP_TAX", new BigDecimal("0.30"));
        BigDecimal employeeTaxedAmount = baseSalary.multiply(employeeTaxRate).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal pensionRate = deductionRates.getOrDefault("PENSION", new BigDecimal("0.06"));
        BigDecimal pensionAmount = baseSalary.multiply(pensionRate).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal medicalInsuranceRate = deductionRates.getOrDefault("MEDICAL_INSURANCE", new BigDecimal("0.05"));
        BigDecimal medicalInsuranceAmount = baseSalary.multiply(medicalInsuranceRate).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal otherRate = deductionRates.getOrDefault("OTHERS", new BigDecimal("0.05"));
        BigDecimal otherTaxedAmount = baseSalary.multiply(otherRate).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total deductions
        BigDecimal totalDeductions = employeeTaxedAmount
                .add(pensionAmount)
                .add(medicalInsuranceAmount)
                .add(otherTaxedAmount)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate net salary
        BigDecimal netSalary = grossSalary.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        
        // Check constraint: net salary should not be negative
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Calculated net salary is negative for employmentId: {}. Setting to zero.", employment.getId());
            netSalary = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        
        // Create payslip entity
        Payslip payslip = Payslip.builder()
                .employment(employment)
                .houseAmount(housingAmount)
                .transportAmount(transportAmount)
                .employeeTaxedAmount(employeeTaxedAmount)
                .pensionAmount(pensionAmount)
                .medicalInsuranceAmount(medicalInsuranceAmount)
                .otherTaxedAmount(otherTaxedAmount)
                .grossSalary(grossSalary)
                .netSalary(netSalary)
                .month(month)
                .year(year)
                .status(PayslipStatus.PENDING)
                // processedDate will be auto-populated by @CreationTimestamp
                .build();
        
        // Save and return
        return payslipRepository.save(payslip);
    }
    
    private PayslipResponseDTO mapToPayslipResponseDTO(Payslip payslip) {
        Employment employment = payslip.getEmployment();
        Employee employee = employment.getEmployee();
        
        return PayslipResponseDTO.builder()
                .id(payslip.getId())
                .employmentId(employment.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeCode(employee.getCode())
                .houseAmount(payslip.getHouseAmount())
                .transportAmount(payslip.getTransportAmount())
                .employeeTaxedAmount(payslip.getEmployeeTaxedAmount())
                .pensionAmount(payslip.getPensionAmount())
                .medicalInsuranceAmount(payslip.getMedicalInsuranceAmount())
                .otherTaxedAmount(payslip.getOtherTaxedAmount())
                .grossSalary(payslip.getGrossSalary())
                .netSalary(payslip.getNetSalary())
                .month(payslip.getMonth())
                .year(payslip.getYear())
                .status(payslip.getStatus())
                .processedDate(payslip.getProcessedDate())
                .paymentDate(payslip.getPaymentDate())
                .build();
    }
}
