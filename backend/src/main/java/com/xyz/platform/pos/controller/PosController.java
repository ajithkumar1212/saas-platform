package com.xyz.platform.pos.controller;

import com.xyz.platform.pos.dto.CreatePosRequest;
import com.xyz.platform.pos.dto.PosResponse;
import com.xyz.platform.pos.service.PosService;
import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "POS Management", description = "Endpoints for managing Point of Sale terminals")
public class PosController {

    private final PosService posService;

    public PosController(PosService posService) {
        this.posService = posService;
    }

    @PostMapping("/api/v1/pos")
    @Operation(summary = "Create a POS terminal under an authorized Store")
    public ResponseEntity<ApiResponse<PosResponse>> createPos(@Valid @RequestBody CreatePosRequest request) {
        PosResponse response = posService.createPos(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "POS terminal created successfully"));
    }

    @GetMapping("/api/v1/stores/{storeId}/pos")
    @Operation(summary = "List authorized POS resources owned by a Store with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<PosResponse>>> listPosByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<PosResponse> response = posService.listPosByStore(storeId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "POS terminals retrieved successfully"));
    }

    @GetMapping("/api/v1/pos/{posId}")
    @Operation(summary = "Read an authorized POS terminal by ID")
    public ResponseEntity<ApiResponse<PosResponse>> getPos(@PathVariable UUID posId) {
        PosResponse response = posService.getPosById(posId);
        return ResponseEntity.ok(ApiResponse.success(response, "POS terminal retrieved successfully"));
    }
}
