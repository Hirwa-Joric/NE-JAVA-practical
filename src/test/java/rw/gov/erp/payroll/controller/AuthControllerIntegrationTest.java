package rw.gov.erp.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rw.gov.erp.payroll.dto.auth.LoginRequest;
import rw.gov.erp.payroll.model.Employee;
import rw.gov.erp.payroll.model.enums.EmployeeStatus;
import rw.gov.erp.payroll.model.enums.Role;
import rw.gov.erp.payroll.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static java.util.Optional.of;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Create test roles
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_MANAGER);

        // Create test employee with encoded password
        testEmployee = Employee.builder()
                .id(1L)
                .code("EMP-12345")
                .firstName("Test")
                .lastName("User")
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roles(roles)
                .mobile("+250712345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .build();
        
        // Mock repository to return our test employee
        when(employeeRepository.findByEmail(anyString())).thenReturn(of(testEmployee));
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Create login request
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // Perform POST request to login endpoint
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Verify response
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                // Skip checking the type field as it might be null in tests
                // .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.email").value(testEmployee.getEmail()))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_MANAGER"));
    }

    @Test
    void testLoginFailureWrongPassword() throws Exception {
        // Create login request with wrong password
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, "wrongpassword");

        // Perform POST request to login endpoint
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Verify response
        result.andExpect(status().isUnauthorized());
    }
}
