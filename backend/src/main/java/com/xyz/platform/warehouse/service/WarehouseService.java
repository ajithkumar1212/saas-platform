package com.xyz.platform.warehouse.service;

import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.store.repository.StoreRepository;
import com.xyz.platform.warehouse.dto.CreateWarehouseRequest;
import com.xyz.platform.warehouse.dto.WarehouseResponse;
import com.xyz.platform.warehouse.entity.Warehouse;
import com.xyz.platform.warehouse.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;

    public WarehouseService(WarehouseRepository warehouseRepository, StoreRepository storeRepository) {
        this.warehouseRepository = warehouseRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }

        if (warehouseRepository.existsByStoreIdAndWarehouseCode(request.getStoreId(), request.getWarehouseCode())) {
            throw new BusinessException("WAREHOUSE_CODE_EXISTS",
                    "Warehouse code '" + request.getWarehouseCode() + "' already exists for this store");
        }

        Warehouse warehouse = Warehouse.builder()
                .warehouseId(UUID.randomUUID())
                .storeId(request.getStoreId())
                .warehouseCode(request.getWarehouseCode().trim())
                .warehouseName(request.getWarehouseName().trim())
                .warehouseType(request.getWarehouseType().trim())
                .status("ACTIVE")
                .isPrimary(request.isPrimary())
                .capacity(request.getCapacity())
                .capacityUnit(request.getCapacityUnit())
                .description(request.getDescription())
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseResponse> listWarehousesByStore(UUID storeId, String search, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Page<Warehouse> page;
        if (search != null && !search.isBlank()) {
            page = warehouseRepository.searchWarehouses(storeId, search.trim(), pageable);
        } else {
            page = warehouseRepository.findByStoreId(storeId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));
        return mapToResponse(warehouse);
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .warehouseId(warehouse.getWarehouseId())
                .storeId(warehouse.getStoreId())
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .warehouseType(warehouse.getWarehouseType())
                .status(warehouse.getStatus())
                .isPrimary(warehouse.isPrimary())
                .capacity(warehouse.getCapacity())
                .capacityUnit(warehouse.getCapacityUnit())
                .description(warehouse.getDescription())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
