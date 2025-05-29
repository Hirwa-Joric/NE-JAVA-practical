package rw.gov.erp.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate joinDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
