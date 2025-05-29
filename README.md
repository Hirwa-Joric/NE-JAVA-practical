# Rwanda ERP - Payroll & Employee Management Backend

A robust and secure backend system for Payroll and Employee Management for the Government of Rwanda's ERP, built using Java and Spring Boot.

## Overview

This application provides a comprehensive solution for managing employees, employment records, payroll generation, and payslip distribution with email notifications. It includes secure authentication and role-based authorization using JWT tokens.

## Features

- **Employee Management**: Create, update, disable, and retrieve employee information
- **Employment Management**: Track employee positions, departments, and salary details
- **Payroll Processing**: Generate monthly payrolls with automated calculations
- **Deduction Management**: Configure and manage tax and benefit deductions
- **Payslip Generation**: Create detailed payslips with all salary components
- **Email Notifications**: Send automated notifications when payroll is approved
- **Role-Based Security**: ROLE_EMPLOYEE, ROLE_MANAGER, and ROLE_ADMIN access levels
- **JWT Authentication**: Secure API access with token-based authentication
- **API Documentation**: Swagger UI for interactive API exploration

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6+ (for building the project)
- PostgreSQL (for production deployment)
- H2 Database (included for development)

## Building the Project

To build the project, run:

```bash
mvn clean install
```

## Running the Project

After building, you can run the application using:

```bash
java -jar target/payroll-service-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The application will start on port 8080 by default.

## Configuration

The application can be configured via `application.properties`. Key properties include:

### Database Configuration

For development (H2 in-memory database):
```properties
spring.datasource.url=jdbc:h2:mem:payrolldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
```

For production (PostgreSQL):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rw_erp_payroll
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### JWT Configuration

```properties
jwt.secret=YourStrongSecretKeyHereMakeItLongAndRandom
jwt.expiration.ms=86400000
```

**IMPORTANT**: Always change the JWT secret key for production deployments.

### Email Configuration

```properties
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=user@example.com
spring.mail.password=password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.email.from=noreply@erp.gov.rw
app.institution.name=Government of Rwanda
```

## API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Security and Roles

The application uses three main roles:

- **ROLE_EMPLOYEE**: Basic access for viewing own details and payslips
- **ROLE_MANAGER**: Can manage employees and employment records, generate payroll
- **ROLE_ADMIN**: Has all manager permissions plus can approve payroll and manage deductions

## Payroll Calculation Logic

Payroll is calculated as follows:

1. **Base Salary**: Defined in the Employment record
2. **Allowances**:
   - Housing: 14% of base salary
   - Transport: 14% of base salary
3. **Gross Salary**: Base Salary + Housing + Transport
4. **Deductions**:
   - Employee Tax: 30% of base salary
   - Pension: 6% of base salary (new rate)
   - Medical Insurance: 5% of base salary
   - Others: 5% of base salary
5. **Net Salary**: Gross Salary - Total Deductions

## Development Notes

- The system uses the H2 in-memory database by default for development
- Pre-configured deductions are loaded at startup
- Email notifications are sent asynchronously to avoid blocking API responses
- Swagger UI provides interactive API documentation

## Testing

Run tests with:

```bash
mvn test
```

## License

Private - Government of Rwanda
