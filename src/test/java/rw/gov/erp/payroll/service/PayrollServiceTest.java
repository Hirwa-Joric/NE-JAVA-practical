package rw.gov.erp.payroll.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.model.Deduction;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Employment;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;
import rw.gov.erp.payroll.model.enums.PayslipStatus;
import rw.gov.erp.payroll.repository.DeductionRepository;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.repository.PayslipRepository;
import rw.gov.erp.payroll.service.impl.PayrollServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private DeductionRepository deductionRepository;

    @Mock
    private PayslipRepository payslipRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee testEmployee;
    private Employment testEmployment;
    private List<Deduction> testDeductions;
    private int testMonth;
    private int testYear;

    @BeforeEach
    void setUp() {
        // Setup test data
        testMonth = 5;
        testYear = 2025;

        // Create test employee
        testEmployee = Employee.builder()
                .id(1L)
                .code("EMP-12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .status(EmployeeStatus.ACTIVE)
                .build();

        // Create test employment with base salary of 1,000,000 RWF
        testEmployment = Employment.builder()
                .id(1L)
                .code("EMPL-12345")
                .employee(testEmployee)
                .department("IT")
                .position("Developer")
                .baseSalary(new BigDecimal("1000000.00"))
                .status(EmploymentStatus.ACTIVE)
                .joinDate(LocalDate.of(2023, 1, 1))
                .build();

        // Create test deductions
        testDeductions = List.of(
                new Deduction(1L, "EMP_TAX", "EmployeeTax", new BigDecimal("0.30"), LocalDateTime.now(), LocalDateTime.now()),
                new Deduction(2L, "PENSION", "Pension", new BigDecimal("0.06"), LocalDateTime.now(), LocalDateTime.now()),
                new Deduction(3L, "MEDICAL_INSURANCE", "MedicalInsurance", new BigDecimal("0.05"), LocalDateTime.now(), LocalDateTime.now()),
                new Deduction(4L, "OTHERS", "Others", new BigDecimal("0.05"), LocalDateTime.now(), LocalDateTime.now()),
                new Deduction(5L, "HOUSING", "Housing", new BigDecimal("0.14"), LocalDateTime.now(), LocalDateTime.now()),
                new Deduction(6L, "TRANSPORT", "Transport", new BigDecimal("0.14"), LocalDateTime.now(), LocalDateTime.now())
        );
    }

    @Test
    void testGeneratePayrollForMonth() {
        // Arrange
        when(deductionRepository.findAll()).thenReturn(testDeductions);
        when(employmentRepository.findByStatus(EmploymentStatus.ACTIVE)).thenReturn(List.of(testEmployment));
        when(payslipRepository.findByEmploymentIdAndMonthAndYear(testEmployment.getId(), testMonth, testYear)).thenReturn(Optional.empty());

        // Mock the saving of a new payslip
        when(payslipRepository.save(any(Payslip.class))).thenAnswer(invocation -> {
            Payslip payslip = invocation.getArgument(0);
            payslip.setId(1L);
            payslip.setProcessedDate(LocalDateTime.now());
            return payslip;
        });

        // Act
        List<PayslipResponseDTO> result = payrollService.generatePayrollForMonth(testMonth, testYear);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PayslipResponseDTO payslipDTO = result.get(0);
        assertEquals(testEmployment.getId(), payslipDTO.getEmploymentId());
        assertEquals(testEmployee.getFirstName() + " " + testEmployee.getLastName(), payslipDTO.getEmployeeName());
        assertEquals(testEmployee.getCode(), payslipDTO.getEmployeeCode());
        assertEquals(PayslipStatus.PENDING, payslipDTO.getStatus());
        
        // Verify calculations
        BigDecimal baseSalary = testEmployment.getBaseSalary();
        BigDecimal housingAmount = baseSalary.multiply(new BigDecimal("0.14")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal transportAmount = baseSalary.multiply(new BigDecimal("0.14")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossSalary = baseSalary.add(housingAmount).add(transportAmount).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal employeeTaxedAmount = baseSalary.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pensionAmount = baseSalary.multiply(new BigDecimal("0.06")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal medicalInsuranceAmount = baseSalary.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal otherTaxedAmount = baseSalary.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalDeductions = employeeTaxedAmount
                .add(pensionAmount)
                .add(medicalInsuranceAmount)
                .add(otherTaxedAmount)
                .setScale(2, RoundingMode.HALF_UP);
                
        BigDecimal netSalary = grossSalary.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        
        assertEquals(housingAmount, payslipDTO.getHouseAmount());
        assertEquals(transportAmount, payslipDTO.getTransportAmount());
        assertEquals(grossSalary, payslipDTO.getGrossSalary());
        assertEquals(employeeTaxedAmount, payslipDTO.getEmployeeTaxedAmount());
        assertEquals(pensionAmount, payslipDTO.getPensionAmount());
        assertEquals(medicalInsuranceAmount, payslipDTO.getMedicalInsuranceAmount());
        assertEquals(otherTaxedAmount, payslipDTO.getOtherTaxedAmount());
        assertEquals(netSalary, payslipDTO.getNetSalary());
    }

    @Test
    void testApproveMonthlyPayroll() {
        // Arrange
        Payslip pendingPayslip = Payslip.builder()
                .id(1L)
                .employment(testEmployment)
                .houseAmount(new BigDecimal("140000.00"))
                .transportAmount(new BigDecimal("140000.00"))
                .employeeTaxedAmount(new BigDecimal("300000.00"))
                .pensionAmount(new BigDecimal("60000.00"))
                .medicalInsuranceAmount(new BigDecimal("50000.00"))
                .otherTaxedAmount(new BigDecimal("50000.00"))
                .grossSalary(new BigDecimal("1280000.00"))
                .netSalary(new BigDecimal("820000.00"))
                .month(testMonth)
                .year(testYear)
                .status(PayslipStatus.PENDING)
                .processedDate(LocalDateTime.now().minusDays(5))
                .build();

        when(payslipRepository.findByMonthAndYearAndStatus(testMonth, testYear, PayslipStatus.PENDING))
                .thenReturn(List.of(pendingPayslip));

        when(payslipRepository.save(any(Payslip.class))).thenAnswer(invocation -> {
            Payslip payslip = invocation.getArgument(0);
            payslip.setPaymentDate(LocalDateTime.now());
            return payslip;
        });

        // Act
        List<PayslipResponseDTO> result = payrollService.approveMonthlyPayroll(testMonth, testYear);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PayslipStatus.PAID, result.get(0).getStatus());
        assertNotNull(result.get(0).getPaymentDate());
        
        // Verify event publisher was called (email notification)
        Mockito.verify(eventPublisher).publishEvent(any());
    }
}
