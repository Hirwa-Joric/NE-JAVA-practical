package rw.gov.erp.payroll.dto.deduction;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeductionRequestDTO {
    
    @NotBlank(message = "Deduction code is required")
    @Pattern(regexp = "^[A-Z_]{3,20}$", message = "Code must be uppercase letters and underscores only, 3-20 characters")
    private String code;
    
    @NotBlank(message = "Deduction name is required")
    @Size(min = 2, max = 50, message = "Deduction name must be between 2 and 50 characters")
    private String deductionName;
    
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Percentage must be non-negative")
    @DecimalMax(value = "1.0", inclusive = true, message = "Percentage must not exceed 100% (1.0)")
    private BigDecimal percentage;
}
