package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.model.Employee;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);
    
    Optional<Employee> findByCode(String code);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByMobile(String mobile);
    
    Boolean existsByCode(String code);
}
