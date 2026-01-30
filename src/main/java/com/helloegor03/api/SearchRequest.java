package com.helloegor03.api;

import com.helloegor03.domain.DocumentStatus;

import java.time.LocalDateTime;

public record SearchRequest(
        DocumentStatus status,
        String author,
        LocalDateTime from,
        LocalDateTime to
) {
}
