package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.NotificationLog;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    List<NotificationLog> findByEmployee(Employee employee);
    
    List<NotificationLog> findByRecipientEmail(String email);
    
    List<NotificationLog> findByStatus(NotificationLog.NotificationStatus status);
}
