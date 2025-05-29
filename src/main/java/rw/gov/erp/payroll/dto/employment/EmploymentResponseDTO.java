package rw.gov.erp.payroll.dto.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentResponseDTO {
    private Long id;
    private String code;
    private Long employeeId;
    private String employeeName; // Concatenated first and last name for convenience
    private String department;
    private String position;
    private BigDecimal baseSalary;
    private EmploymentStatus status;
    private LocalDate joinDate;
}
