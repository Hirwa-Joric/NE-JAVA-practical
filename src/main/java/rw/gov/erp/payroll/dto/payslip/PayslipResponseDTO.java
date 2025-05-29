package rw.gov.erp.payroll.dto.payslip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.gov.erp.payroll.model.enums.PayslipStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayslipResponseDTO {
    private Long id;
    private Long employmentId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal employeeTaxedAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxedAmount;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private int month;
    private int year;
    private PayslipStatus status;
    private LocalDateTime processedDate;
    private LocalDateTime paymentDate;
}
