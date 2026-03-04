package br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomError> handleResourceNotFound(
        ResourceNotFoundException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomError err = new CustomError(
            Instant.now(),
            status.value(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<CustomError> handleBusinessRule(
        BusinessRuleException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        CustomError err = new CustomError(
            Instant.now(),
            status.value(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationExceptions(
        MethodArgumentNotValidException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        ValidationError err = new ValidationError(
            Instant.now(),
            status.value(),
            "Invalid data",
            request.getRequestURI()
        );

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomError> handleAuthenticationException(
        AuthenticationException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        CustomError err = new CustomError(
            Instant.now(),
            status.value(),
            "Invalid credentials",
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }
}
