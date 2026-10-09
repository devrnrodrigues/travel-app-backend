package com.devrenanrodrigues.travelapi.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        if (password.length() < 8) {
            return buildViolation(context, "A senha deve conter no mínimo 8 caracteres.");
        }

        if (password.length() > 72) {
            return buildViolation(context, "A senha deve conter no máximo 72 caracteres.");
        }

        if (password.matches(".*\\s.*")) {
            return buildViolation(context, "A senha não pode conter espaços.");
        }

        if (!password.matches(".*[A-Z].*")) {
            return buildViolation(context, "A senha deve conter pelo menos uma letra maiúscula.");
        }

        if (!password.matches(".*[a-z].*")) {
            return buildViolation(context, "A senha deve conter pelo menos uma letra minúscula.");
        }

        if (!password.matches(".*\\d.*")) {
            return buildViolation(context, "A senha deve conter pelo menos um número.");
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return buildViolation(context, "A senha deve conter pelo menos um caractere especial.");
        }

        return true;
    }

    private boolean buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
