package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.model.Deduction;

import java.util.Optional;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, Long> {
    
    Optional<Deduction> findByCode(String code);
    
    Optional<Deduction> findByDeductionName(String deductionName);
}
