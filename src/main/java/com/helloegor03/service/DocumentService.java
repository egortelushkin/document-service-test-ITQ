package com.helloegor03.service;

import com.helloegor03.Exception.NotFoundException;
import com.helloegor03.api.*;
import com.helloegor03.domain.*;
import com.helloegor03.mapper.DocumentMapper;
import com.helloegor03.mapper.HistoryMapper;
import com.helloegor03.repository.ApprovalRegistryRepository;
import com.helloegor03.repository.DocumentHistoryRepository;
import com.helloegor03.repository.DocumentRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final DocumentMapper documentMapper;
    private final HistoryMapper historyMapper;

    @Transactional
    public Long create(CreateDocumentRequest request) {

        Document doc = documentMapper.fromCreateRequest(request);

        doc.setNumber(UUID.randomUUID().toString());
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

    public DocumentWithHistoryResponse getWithHistory(Long id) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        List<DocumentHistory> history =
                historyRepository.findByDocumentIdOrderByCreatedAtAsc(id);

        return new DocumentWithHistoryResponse(
                documentMapper.toDto(doc),
                history.stream().map(historyMapper::toDto).toList()
        );
    }

    public Page<DocumentResponse> getBatch(List<Long> ids, Pageable pageable) {
        return documentRepository.findAllByIdIn(ids, pageable)
                .map(DocumentResponse::from);
    }

    public List<BatchResultDto> approveBatch(List<Long> ids, String actor) {
        return ids.stream()
                .map(id -> {
                    try {
                        return new BatchResultDto(id, approve(id, actor));
                    } catch (OptimisticLockException e) {
                        return new BatchResultDto(id, CONFLICT);
                    } catch (RuntimeException e) {
                        return new BatchResultDto(id, REGISTRY_ERROR);
                    }
                })
                .toList();
    }

    public Page<DocumentResponse> search(SearchRequest request, Pageable pageable) {
        Specification<Document> spec = Specification.where(null);

        if (request.status() != null) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("status"), request.status()));
        }

        if (request.author() != null) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("author"), request.author()));
        }

        if (request.from() != null && request.to() != null) {
            spec = spec.and((root, q, cb) ->
                    cb.between(root.get("updatedAt"), request.from(), request.to()));
        }

        return documentRepository.findAll(spec, pageable)
                .map(DocumentResponse::from);
    }
}
