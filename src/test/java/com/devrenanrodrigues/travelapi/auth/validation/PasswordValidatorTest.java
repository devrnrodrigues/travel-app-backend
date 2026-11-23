package com.devrenanrodrigues.travelapi.auth.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private PasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
    }

    private void mockViolationBuilder() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve considerar válido quando senha for nula, vazia ou em branco (delega para @NotBlank)")
    void shouldReturnTrueWhenPasswordIsNullOrEmpty(String password) {
        boolean isValid = validator.isValid(password, context);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve validar com sucesso uma senha forte com todos os requisitos")
    void shouldReturnTrueWhenPasswordMeetsAllRequirements() {
        boolean isValid = validator.isValid("SenhaForte@2026", context);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve falhar quando a senha tiver menos de 8 caracteres")
    void shouldReturnFalseWhenPasswordIsTooShort() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("Ab1!xyz", context);

        assertThat(isValid).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter no mínimo 8 caracteres.");
    }

    @Test
    @DisplayName("Deve falhar quando a senha tiver mais de 72 caracteres")
    void shouldReturnFalseWhenPasswordIsTooLong() {
        mockViolationBuilder();
        String longPassword = "A1!" + "a".repeat(70);
        boolean isValid = validator.isValid(longPassword, context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter no máximo 72 caracteres.");
    }

    @Test
    @DisplayName("Deve falhar quando a senha contiver espaços")
    void shouldReturnFalseWhenPasswordContainsSpaces() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("Senha Forte1!", context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha não pode conter espaços.");
    }

    @Test
    @DisplayName("Deve falhar quando não houver letra maiúscula")
    void shouldReturnFalseWhenPasswordLacksUppercase() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("senhaforte@123", context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter pelo menos uma letra maiúscula.");
    }

    @Test
    @DisplayName("Deve falhar quando não houver letra minúscula")
    void shouldReturnFalseWhenPasswordLacksLowercase() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("SENHAFORTE@123", context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter pelo menos uma letra minúscula.");
    }

    @Test
    @DisplayName("Deve falhar quando não houver número")
    void shouldReturnFalseWhenPasswordLacksDigit() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("SenhaForte@XYZ", context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter pelo menos um número.");
    }

    @Test
    @DisplayName("Deve falhar quando não houver caractere especial")
    void shouldReturnFalseWhenPasswordLacksSpecialCharacter() {
        mockViolationBuilder();
        boolean isValid = validator.isValid("SenhaForte1234", context);

        assertThat(isValid).isFalse();
        verify(context).buildConstraintViolationWithTemplate("A senha deve conter pelo menos um caractere especial.");
    }
}
