package com.xyz.platform.channel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotBlank(message = "Channel code is required")
    @Size(min = 2, max = 50, message = "Channel code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Channel code must be alphanumeric with dashes or underscores")
    private String channelCode;

    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 150, message = "Channel name must be between 2 and 150 characters")
    private String channelName;

    @NotBlank(message = "Channel type is required")
    @Size(max = 30, message = "Channel type must not exceed 30 characters")
    private String channelType;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
