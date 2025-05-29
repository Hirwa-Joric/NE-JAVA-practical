package rw.gov.erp.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deductions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(unique = true, nullable = false)
    private String deductionName;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
