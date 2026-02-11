package com.common.util;

import com.common.dto.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationUtilsTest {

    @Test
    void testGetValidationErrors_withMultipleErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = List.of(
                new FieldError("user", "email", "Email is required"),
                new FieldError("user", "password", "Password must be at least 8 characters"),
                new FieldError("user", "username", "Username is already taken")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        List<ValidationError> result = ValidationUtils.getValidationErrors(bindingResult);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        assertEquals("email", result.get(0).getField());
        assertEquals("Email is required", result.get(0).getMessage());
        
        assertEquals("password", result.get(1).getField());
        assertEquals("Password must be at least 8 characters", result.get(1).getMessage());
        
        assertEquals("username", result.get(2).getField());
        assertEquals("Username is already taken", result.get(2).getMessage());
    }

    @Test
    void testGetValidationErrors_withNoErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        List<ValidationError> result = ValidationUtils.getValidationErrors(bindingResult);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetValidationErrors_withSingleError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = List.of(
                new FieldError("tournament", "name", "Name cannot be empty")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        List<ValidationError> result = ValidationUtils.getValidationErrors(bindingResult);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("name", result.get(0).getField());
        assertEquals("Name cannot be empty", result.get(0).getMessage());
    }
}
