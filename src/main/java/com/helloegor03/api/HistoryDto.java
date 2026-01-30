package com.helloegor03.api;

import com.helloegor03.domain.DocumentAction;
import com.helloegor03.domain.DocumentHistory;

import java.time.LocalDateTime;

public record HistoryDto(
        DocumentAction action,
        String actor,
        String comment,
        LocalDateTime createdAt
) {
    public static HistoryDto from(DocumentHistory h) {
        return new HistoryDto(
                h.getAction(),
                h.getActor(),
                h.getComment(),
                h.getCreatedAt()
        );
    }
}