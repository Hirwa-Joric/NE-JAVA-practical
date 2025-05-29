package rw.gov.erp.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.gov.erp.payroll.dto.auth.JwtResponse;
import rw.gov.erp.payroll.dto.auth.LoginRequest;
import rw.gov.erp.payroll.dto.auth.SignupRequest;
import rw.gov.erp.payroll.dto.auth.MessageResponse;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.model.enums.Role;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.security.jwt.JwtUtils;
import rw.gov.erp.payroll.security.services.UserDetailsImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                roles));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Check if email exists
        if (employeeRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        Set<Role> roles = new HashSet<>();
        
        if (signupRequest.getRoles() != null && !signupRequest.getRoles().isEmpty()) {
            signupRequest.getRoles().forEach(role -> {
                try {
                    roles.add(Role.valueOf(role));
                } catch (IllegalArgumentException e) {
                    // If role is not valid, ignore it
                }
            });
        }
        
        // If no valid roles provided, default to ROLE_EMPLOYEE
        if (roles.isEmpty()) {
            roles.add(Role.ROLE_EMPLOYEE);
        }

        // Generate employee code (simple implementation)
        String employeeCode = "EMP-" + System.currentTimeMillis() % 10000;
        
        // Create employee
        Employee employee = Employee.builder()
                .code(employeeCode)
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .mobile(signupRequest.getMobile())
                .dateOfBirth(signupRequest.getDateOfBirth() != null ? signupRequest.getDateOfBirth() : LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        employeeRepository.save(employee);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
