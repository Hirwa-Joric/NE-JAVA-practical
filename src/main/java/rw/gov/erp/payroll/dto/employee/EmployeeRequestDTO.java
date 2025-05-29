package rw.gov.erp.payroll.dto.employee;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.model.enums.Role;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestDTO {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotEmpty(message = "At least one role must be assigned")
    private Set<Role> roles = new HashSet<>();
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "(\\+?\\d{10,15})", message = "Mobile number must be valid")
    private String mobile;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    // Optional, if provided it will be used, otherwise auto-generated
    private String code;
}
