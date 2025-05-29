package rw.gov.erp.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import rw.gov.erp.payroll.model.enums.PayslipStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslips", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employment_id", "month", "year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employment_id", nullable = false)
    private Employment employment;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal houseAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal transportAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal employeeTaxedAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pensionAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal medicalInsuranceAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal otherTaxedAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status = PayslipStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime processedDate;

    private LocalDateTime paymentDate;
}
