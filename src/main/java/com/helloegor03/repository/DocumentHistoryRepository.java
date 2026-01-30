package com.helloegor03.repository;

import com.helloegor03.domain.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {
    List<DocumentHistory> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}
