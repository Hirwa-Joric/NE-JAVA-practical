package rw.gov.erp.payroll.dto.deduction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeductionResponseDTO {
    private Long id;
    private String code;
    private String deductionName;
    private BigDecimal percentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
