package com.devoteeportal.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkActionRequest {
    private List<UUID> userIds;
    private String reason;
}
