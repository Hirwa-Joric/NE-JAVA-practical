package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.model.Payslip;
import rw.gov.erp.payroll.model.enums.PayslipStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    
    List<Payslip> findByEmploymentEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
    
    Optional<Payslip> findByEmploymentIdAndMonthAndYear(Long employmentId, int month, int year);
    
    List<Payslip> findByMonthAndYear(int month, int year);
    
    List<Payslip> findByMonthAndYearAndStatus(int month, int year, PayslipStatus status);
}
