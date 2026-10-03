package com.xyz.platform.pos.service;

import com.xyz.platform.pos.dto.CreatePosRequest;
import com.xyz.platform.pos.dto.PosResponse;
import com.xyz.platform.pos.entity.PosTerminal;
import com.xyz.platform.pos.repository.PosTerminalRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PosService {

    private final PosTerminalRepository posTerminalRepository;
    private final StoreRepository storeRepository;

    public PosService(PosTerminalRepository posTerminalRepository, StoreRepository storeRepository) {
        this.posTerminalRepository = posTerminalRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public PosResponse createPos(CreatePosRequest request) {
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }

        if (posTerminalRepository.existsByStoreIdAndPosCode(request.getStoreId(), request.getPosCode())) {
            throw new BusinessException("POS_CODE_EXISTS", 
                    "POS code '" + request.getPosCode() + "' already exists for this store");
        }

        PosTerminal pos = PosTerminal.builder()
                .posId(UUID.randomUUID())
                .storeId(request.getStoreId())
                .storeLocationId(request.getStoreLocationId())
                .posCode(request.getPosCode().trim())
                .posName(request.getPosName().trim())
                .deviceIdentifier(request.getDeviceIdentifier())
                .isPrimary(request.isPrimary())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        PosTerminal saved = posTerminalRepository.save(pos);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PosResponse> listPosByStore(UUID storeId, String search, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Page<PosTerminal> page;
        if (search != null && !search.isBlank()) {
            page = posTerminalRepository.searchPos(storeId, search.trim(), pageable);
        } else {
            page = posTerminalRepository.findByStoreId(storeId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PosResponse getPosById(UUID posId) {
        PosTerminal pos = posTerminalRepository.findById(posId)
                .orElseThrow(() -> new ResourceNotFoundException("PosTerminal", posId));
        return mapToResponse(pos);
    }

    private PosResponse mapToResponse(PosTerminal pos) {
        return PosResponse.builder()
                .posId(pos.getPosId())
                .storeId(pos.getStoreId())
                .storeLocationId(pos.getStoreLocationId())
                .posCode(pos.getPosCode())
                .posName(pos.getPosName())
                .deviceIdentifier(pos.getDeviceIdentifier())
                .isPrimary(pos.isPrimary())
                .status(pos.getStatus())
                .description(pos.getDescription())
                .createdAt(pos.getCreatedAt())
                .updatedAt(pos.getUpdatedAt())
                .build();
    }
}
