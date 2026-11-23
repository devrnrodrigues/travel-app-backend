package com.devrenanrodrigues.travelapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Deve tratar ResponseStatusException mantendo status code e mensagem customizada")
    void shouldHandleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleResponseStatusException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Recurso não encontrado");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException extraindo erros de campo")
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("dto", "email", "E-mail inválido");
        FieldError fieldError2 = new FieldError("dto", "password", "A senha é obrigatória");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Erro de validação");
        assertThat(response.getBody().fieldErrors()).containsEntry("email", "E-mail inválido");
        assertThat(response.getBody().fieldErrors()).containsEntry("password", "A senha é obrigatória");
    }

    @Test
    @DisplayName("Deve tratar HttpRequestMethodNotSupportedException com status 405 Method Not Allowed")
    void shouldHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(405);
        assertThat(response.getBody().message()).contains("O método HTTP POST não é suportado");
    }

    @Test
    @DisplayName("Deve tratar HttpMediaTypeNotSupportedException com status 415 Unsupported Media Type")
    void shouldHandleHttpMediaTypeNotSupportedException() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleHttpMediaTypeNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(415);
        assertThat(response.getBody().message()).contains("application/xml");
    }

    @Test
    @DisplayName("Deve tratar MissingServletRequestParameterException com status 400 Bad Request")
    void shouldHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("page", "int");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleMissingServletRequestParameter(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("O parâmetro obrigatório 'page' não foi informado");
    }

    @Test
    @DisplayName("Deve tratar DataIntegrityViolationException com status 400 Bad Request")
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique constraint violation");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleDataIntegrityViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Violação de integridade");
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com status 400 Bad Request")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Valor informado é inválido");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Valor informado é inválido");
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com status 500 Internal Server Error")
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Falha inesperada no banco");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Erro interno");
    }
}
