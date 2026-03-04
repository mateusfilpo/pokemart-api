package br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ValidationError extends CustomError {
    
    private List<FieldMessage> errors = new ArrayList<>();

    public ValidationError(Instant timestamp, Integer status, String error, String path) {
        super(timestamp, status, error, path);
    }

    public void addError(String fieldName, String message) {
        errors.add(new FieldMessage(fieldName, message));
    }
}