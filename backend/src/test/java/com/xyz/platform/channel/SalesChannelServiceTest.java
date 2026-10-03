package com.xyz.platform.channel;

import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.channel.dto.StoreChannelResponse;
import com.xyz.platform.channel.entity.SalesChannel;
import com.xyz.platform.channel.entity.StoreChannel;
import com.xyz.platform.channel.entity.StoreChannelId;
import com.xyz.platform.channel.repository.SalesChannelRepository;
import com.xyz.platform.channel.repository.StoreChannelRepository;
import com.xyz.platform.channel.service.SalesChannelService;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.store.entity.Store;
import com.xyz.platform.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesChannelServiceTest {

    @Mock
    private SalesChannelRepository salesChannelRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreChannelRepository storeChannelRepository;

    @InjectMocks
    private SalesChannelService salesChannelService;

    @Test
    void shouldPermitStoreChannelAssociationWhenSharingSameBusiness() {
        UUID sharedBusinessId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        Store store = Store.builder()
                .storeId(storeId)
                .businessId(sharedBusinessId)
                .storeCode("STR_01")
                .storeName("Flagship Store")
                .build();

        SalesChannel channel = SalesChannel.builder()
                .channelId(channelId)
                .businessId(sharedBusinessId)
                .channelCode("CHN_01")
                .channelName("Online Web Store")
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(salesChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(storeChannelRepository.save(any(StoreChannel.class))).thenAnswer(inv -> {
            StoreChannel sc = inv.getArgument(0);
            sc.onPrePersist();
            return sc;
        });

        StoreChannelResponse response = salesChannelService.associateStoreChannel(storeId, channelId, true, "Primary channel");

        assertNotNull(response);
        assertEquals(storeId, response.getStoreId());
        assertEquals(channelId, response.getChannelId());
        assertTrue(response.isPrimary());
        verify(storeChannelRepository).save(any(StoreChannel.class));
    }

    @Test
    void shouldRejectCrossBusinessStoreChannelAssociation() {
        UUID businessA = UUID.randomUUID();
        UUID businessB = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        Store store = Store.builder()
                .storeId(storeId)
                .businessId(businessA)
                .storeCode("STR_01")
                .build();

        SalesChannel channel = SalesChannel.builder()
                .channelId(channelId)
                .businessId(businessB) // Different business!
                .channelCode("CHN_02")
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(salesChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                salesChannelService.associateStoreChannel(storeId, channelId, false, "Cross-business test"));

        assertEquals("CROSS_BUSINESS_ASSOCIATION_FORBIDDEN", ex.getErrorCode());
        verify(storeChannelRepository, never()).save(any());
    }
}
