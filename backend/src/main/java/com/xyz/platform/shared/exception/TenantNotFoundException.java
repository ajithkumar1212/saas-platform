package com.xyz.platform.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantNotFoundException extends BusinessException {

    public TenantNotFoundException(String message) {
        super("TENANT_NOT_FOUND", message);
    }
}
