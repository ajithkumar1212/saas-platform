package com.xyz.platform.channel.service;

import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.channel.dto.ChannelResponse;
import com.xyz.platform.channel.dto.CreateChannelRequest;
import com.xyz.platform.channel.dto.StoreChannelResponse;
import com.xyz.platform.channel.entity.SalesChannel;
import com.xyz.platform.channel.entity.StoreChannel;
import com.xyz.platform.channel.entity.StoreChannelId;
import com.xyz.platform.channel.repository.SalesChannelRepository;
import com.xyz.platform.channel.repository.StoreChannelRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.store.entity.Store;
import com.xyz.platform.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SalesChannelService {

    private final SalesChannelRepository salesChannelRepository;
    private final BusinessRepository businessRepository;
    private final StoreRepository storeRepository;
    private final StoreChannelRepository storeChannelRepository;

    public SalesChannelService(SalesChannelRepository salesChannelRepository,
                               BusinessRepository businessRepository,
                               StoreRepository storeRepository,
                               StoreChannelRepository storeChannelRepository) {
        this.salesChannelRepository = salesChannelRepository;
        this.businessRepository = businessRepository;
        this.storeRepository = storeRepository;
        this.storeChannelRepository = storeChannelRepository;
    }

    @Transactional
    public ChannelResponse createChannel(CreateChannelRequest request) {
        if (!businessRepository.existsById(request.getBusinessId())) {
            throw new ResourceNotFoundException("Business", request.getBusinessId());
        }

        if (salesChannelRepository.existsByBusinessIdAndChannelCode(request.getBusinessId(), request.getChannelCode())) {
            throw new BusinessException("CHANNEL_CODE_EXISTS",
                    "Channel code '" + request.getChannelCode() + "' already exists for this business");
        }

        SalesChannel channel = SalesChannel.builder()
                .channelId(UUID.randomUUID())
                .businessId(request.getBusinessId())
                .channelCode(request.getChannelCode().trim())
                .channelName(request.getChannelName().trim())
                .channelType(request.getChannelType().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        SalesChannel saved = salesChannelRepository.save(channel);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChannelResponse> listChannelsByBusiness(UUID businessId, String search, Pageable pageable) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }
        Page<SalesChannel> page;
        if (search != null && !search.isBlank()) {
            page = salesChannelRepository.searchChannels(businessId, search.trim(), pageable);
        } else {
            page = salesChannelRepository.findByBusinessId(businessId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public ChannelResponse getChannelById(UUID channelId) {
        SalesChannel channel = salesChannelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesChannel", channelId));
        return mapToResponse(channel);
    }

    @Transactional
    public StoreChannelResponse associateStoreChannel(UUID storeId, UUID channelId, boolean isPrimary, String description) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));

        SalesChannel channel = salesChannelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesChannel", channelId));

        // Strict same-business validation rule enforced as per ADR and 04-api-final.md
        if (!store.getBusinessId().equals(channel.getBusinessId())) {
            throw new BusinessException("CROSS_BUSINESS_ASSOCIATION_FORBIDDEN",
                    String.format("Store (business %s) and Channel (business %s) must belong to the same business",
                            store.getBusinessId(), channel.getBusinessId()));
        }

        StoreChannel association = StoreChannel.builder()
                .id(new StoreChannelId(storeId, channelId))
                .status("ACTIVE")
                .isPrimary(isPrimary)
                .description(description)
                .build();

        StoreChannel saved = storeChannelRepository.save(association);

        return StoreChannelResponse.builder()
                .storeId(saved.getId().getStoreId())
                .channelId(saved.getId().getChannelId())
                .status(saved.getStatus())
                .isPrimary(saved.isPrimary())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private ChannelResponse mapToResponse(SalesChannel channel) {
        return ChannelResponse.builder()
                .channelId(channel.getChannelId())
                .businessId(channel.getBusinessId())
                .channelCode(channel.getChannelCode())
                .channelName(channel.getChannelName())
                .channelType(channel.getChannelType())
                .status(channel.getStatus())
                .description(channel.getDescription())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }
}
