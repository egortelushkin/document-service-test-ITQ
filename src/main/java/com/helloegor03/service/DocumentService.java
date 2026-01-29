package com.helloegor03.service;

import com.helloegor03.api.BatchResultDto;
import com.helloegor03.api.OperationResult;
import com.helloegor03.domain.*;
import com.helloegor03.repository.ApprovalRegistryRepository;
import com.helloegor03.repository.DocumentHistoryRepository;
import com.helloegor03.repository.DocumentRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.helloegor03.api.OperationResult.*;
import static com.helloegor03.domain.DocumentStatus.*;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository historyRepository;
    private final ApprovalRegistryRepository registryRepository;

    @Transactional
    public Long create(String author, String title) {
        Document doc = new Document();
        doc.setNumber(UUID.randomUUID().toString());
        doc.setAuthor(author);
        doc.setTitle(title);
        doc.setStatus(DRAFT);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(doc);
        return doc.getId();
    }

    @Transactional
    public OperationResult submit(Long id, String actor) {
        Optional<Document> opt = documentRepository.findById(id);
        if (opt.isEmpty()) return NOT_FOUND;

        Document doc = opt.get();
        if (doc.getStatus() != DRAFT) return CONFLICT;

        doc.setStatus(SUBMITTED);
        doc.setUpdatedAt(LocalDateTime.now());

        historyRepository.save(
                new DocumentHistory(
                        null,
                        doc.getId(),
                        actor,
                        DocumentAction.SUBMIT,
                        null,
                        LocalDateTime.now()
                )
        );

        return SUCCESS;
    }

    @Transactional
    public OperationResult approve(Long id, String actor) {
        Document doc = documentRepository.findById(id)
                .orElse(null);

        if (doc == null) return NOT_FOUND;
        if (doc.getStatus() != SUBMITTED) return CONFLICT;

        doc.setStatus(APPROVED);
        doc.setUpdatedAt(LocalDateTime.now());

        historyRepository.save(
                new DocumentHistory(
                        null,
                        doc.getId(),
                        actor,
                        DocumentAction.APPROVE,
                        null,
                        LocalDateTime.now()
                )
        );

        try {
            registryRepository.save(
                    new ApprovalRegistry(
                            null,
                            doc.getId(),
                            LocalDateTime.now()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Registry error");
        }

        return SUCCESS;
    }

    public List<BatchResultDto> submitBatch(List<Long> ids, String actor) {
        return ids.stream()
                .map(id -> {
                    try {
                        return new BatchResultDto(id, submit(id, actor));
                    } catch (OptimisticLockException e) {
                        return new BatchResultDto(id, CONFLICT);
                    }
                })
                .toList();
    }
}
