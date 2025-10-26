// src/main/java/com/tourney/common/util/ValidationUtils.java
package com.common.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import com.common.dto.ValidationError;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationUtils {
    public static List<ValidationError> getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
    }
}