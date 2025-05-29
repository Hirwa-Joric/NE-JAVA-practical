package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.Employment;
import rw.gov.erp.payroll.model.enums.EmploymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {
    
    List<Employment> findByEmployeeAndStatus(Employee employee, EmploymentStatus status);
    
    List<Employment> findByStatus(EmploymentStatus status);
    
    Optional<Employment> findByCode(String code);
}
