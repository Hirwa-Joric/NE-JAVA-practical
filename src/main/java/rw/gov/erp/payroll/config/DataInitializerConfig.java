package rw.gov.erp.payroll.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rw.gov.erp.payroll.model.Deduction;
import rw.gov.erp.payroll.repository.DeductionRepository;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    @Bean
    CommandLineRunner initDatabase(DeductionRepository repository) {
        return args -> {
            // Check if deductions exist before adding
            if (repository.findByCode("EMP_TAX").isEmpty()) {
                repository.save(new Deduction(null, "EMP_TAX", "EmployeeTax", new BigDecimal("0.30"), null, null));
            }
            
            if (repository.findByCode("PENSION").isEmpty()) {
                repository.save(new Deduction(null, "PENSION", "Pension", new BigDecimal("0.06"), null, null));
            }
            
            if (repository.findByCode("MEDICAL_INSURANCE").isEmpty()) {
                repository.save(new Deduction(null, "MEDICAL_INSURANCE", "MedicalInsurance", new BigDecimal("0.05"), null, null));
            }
            
            if (repository.findByCode("OTHERS").isEmpty()) {
                repository.save(new Deduction(null, "OTHERS", "Others", new BigDecimal("0.05"), null, null));
            }
            
            if (repository.findByCode("HOUSING").isEmpty()) {
                repository.save(new Deduction(null, "HOUSING", "Housing", new BigDecimal("0.14"), null, null));
            }
            
            if (repository.findByCode("TRANSPORT").isEmpty()) {
                repository.save(new Deduction(null, "TRANSPORT", "Transport", new BigDecimal("0.14"), null, null));
            }
        };
    }
}
