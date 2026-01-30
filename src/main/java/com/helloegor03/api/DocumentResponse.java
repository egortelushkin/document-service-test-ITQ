package com.helloegor03.api;

import com.helloegor03.domain.Document;
import com.helloegor03.domain.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String number,
        String author,
        String title,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getNumber(),
                d.getAuthor(),
                d.getTitle(),
                d.getStatus(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}