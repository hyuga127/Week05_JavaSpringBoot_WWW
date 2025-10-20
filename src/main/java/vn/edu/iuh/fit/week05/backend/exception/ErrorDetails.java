package vn.edu.iuh.fit.week05.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private String path;
    private Map<String, String> validationErrors;
    
    public ErrorDetails(LocalDateTime timestamp, String message, String details, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.path = path;
    }
}
