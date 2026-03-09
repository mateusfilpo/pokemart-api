package br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        // Toda exceção capturada pelo nosso Handler precisa saber de qual rota veio o erro
        when(request.getRequestURI()).thenReturn("/api/v1/pokemart");
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found e CustomError para ResourceNotFoundException")
    void shouldHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Item não encontrado");

        // Act
        ResponseEntity<CustomError> response = exceptionHandler.handleResourceNotFound(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        CustomError errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(404, errorBody.getStatus());
        assertEquals("Item não encontrado", errorBody.getError());
        assertEquals("/api/v1/pokemart", errorBody.getPath());
        assertNotNull(errorBody.getTimestamp());
    }

    @Test
    @DisplayName("Deve retornar 422 Unprocessable Content e CustomError para BusinessRuleException")
    void shouldHandleBusinessRuleException() {
        // Arrange
        BusinessRuleException exception = new BusinessRuleException("Estoque insuficiente");

        // Act
        ResponseEntity<CustomError> response = exceptionHandler.handleBusinessRule(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        
        CustomError errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(422, errorBody.getStatus());
        assertEquals("Estoque insuficiente", errorBody.getError());
        assertEquals("/api/v1/pokemart", errorBody.getPath());
    }

    @Test
    @DisplayName("Deve retornar 422 Unprocessable Content e ValidationError para MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Arrange
        // Criamos o erro do Spring simulando que o usuário mandou um preço negativo
        FieldError fieldError1 = new FieldError("ItemRequestDTO", "price", "Price must be greater than zero.");
        FieldError fieldError2 = new FieldError("ItemRequestDTO", "name", "Item name is required.");
        
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ValidationError> response = exceptionHandler.handleValidationExceptions(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        
        ValidationError errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(422, errorBody.getStatus());
        assertEquals("Invalid data", errorBody.getError());
        assertEquals("/api/v1/pokemart", errorBody.getPath());
        
        // Verifica se a lista de erros de campo foi populada corretamente
        assertEquals(2, errorBody.getErrors().size());
        assertEquals("price", errorBody.getErrors().get(0).fieldName());
        assertEquals("Price must be greater than zero.", errorBody.getErrors().get(0).message());
        assertEquals("name", errorBody.getErrors().get(1).fieldName());
        assertEquals("Item name is required.", errorBody.getErrors().get(1).message());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized e mensagem fixa para AuthenticationException")
    void shouldHandleAuthenticationException() {
        // Arrange
        // AuthenticationException é abstrata, então usamos mock
        AuthenticationException exception = mock(AuthenticationException.class);

        // Act
        ResponseEntity<CustomError> response = exceptionHandler.handleAuthenticationException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        CustomError errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(401, errorBody.getStatus());
        assertEquals("Invalid credentials", errorBody.getError()); // Mensagem fixa de segurança
        assertEquals("/api/v1/pokemart", errorBody.getPath());
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden para AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Acesso negado, apenas ADMINs.");

        // Act
        ResponseEntity<CustomError> response = exceptionHandler.handleAccessDeniedException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        CustomError errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(403, errorBody.getStatus());
        assertEquals("Acesso negado, apenas ADMINs.", errorBody.getError());
        assertEquals("/api/v1/pokemart", errorBody.getPath());
    }
}