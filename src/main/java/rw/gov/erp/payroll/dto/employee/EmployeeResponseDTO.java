package rw.gov.erp.payroll.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.model.enums.Role;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String code;
    private String firstName;
    private String lastName;
    private String email;
    private Set<Role> roles;
    private String mobile;
    private LocalDate dateOfBirth;
    private EmployeeStatus status;
}
