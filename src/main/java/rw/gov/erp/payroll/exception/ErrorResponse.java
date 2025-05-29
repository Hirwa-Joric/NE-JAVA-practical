package rw.gov.erp.payroll.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    
    private String error;
    
    private String message;
    
    private String path;
    
    private List<ValidationError> validationErrors = new ArrayList<>();
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
