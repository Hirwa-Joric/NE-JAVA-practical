package rw.gov.erp.payroll.dto.employment;

import jakarta.validation.constraints.*;
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
public class EmploymentRequestDTO {
    
    // Either employeeId or employeeCode must be provided
    private Long employeeId;
    
    private String employeeCode;
    
    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;
    
    @NotBlank(message = "Position is required")
    @Size(min = 2, max = 100, message = "Position must be between 2 and 100 characters")
    private String position;
    
    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    private BigDecimal baseSalary;
    
    private EmploymentStatus status = EmploymentStatus.ACTIVE;
    
    @NotNull(message = "Join date is required")
    @PastOrPresent(message = "Join date must be in the past or present")
    private LocalDate joinDate;
}
