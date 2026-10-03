package com.xyz.platform.shared.exception;

import com.xyz.platform.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/organizations");
    }

    @Test
    void shouldHandleTenantNotFoundExceptionAsForbidden() {
        TenantNotFoundException ex = new TenantNotFoundException("Tenant not recognized: acme");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleTenantNotFound(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("TENANT_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Tenant not recognized: acme", response.getBody().getMessage());
        assertEquals("/api/v1/organizations", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleResourceNotFoundExceptionAsNotFound() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Company", id);
        request.setRequestURI("/api/v1/companies/" + id);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains(id.toString()));
        assertEquals("/api/v1/companies/" + id, response.getBody().getPath());
    }

    @Test
    void shouldHandleBusinessExceptionAsBadRequest() {
        BusinessException ex = new BusinessException("BUSINESS_CODE_EXISTS", "Business code exists");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("BUSINESS_CODE_EXISTS", response.getBody().getErrorCode());
        assertEquals("Business code exists", response.getBody().getMessage());
    }

    @Test
    void shouldHandleGenericExceptionAsInternalServerError() {
        Exception ex = new RuntimeException("Unexpected DB connection drop");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("unexpected"));
    }
}
