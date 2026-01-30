package com.helloegor03.repository;

import com.helloegor03.domain.Document;
import com.helloegor03.domain.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @Query("select d.id from Document d where d.status = :status")
    Page<Long> findIdsByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    Page<Document> findAllByIdIn(List<Long> ids, Pageable pageable);

    List<Document> findByStatus(DocumentStatus status, Pageable pageable);

    @Query("select d from Document d where d.status = :status order by d.id asc")
    List<Document> findNextBatchByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    @Modifying
    @Query(value = """
        UPDATE documents
        SET status = 'SUBMITTED', updated_at = NOW(), locked_at = NOW()
        WHERE id IN (
            SELECT id FROM documents
            WHERE status = 'DRAFT' AND (locked_at IS NULL OR locked_at < NOW() - INTERVAL '10 MINUTE')
            ORDER BY id ASC
            LIMIT :batchSize
        )
        RETURNING id
        """, nativeQuery = true)
    List<Long> claimDraftBatch(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
        UPDATE documents
        SET status = 'APPROVED', updated_at = NOW(), locked_at = NOW()
        WHERE id IN (
            SELECT id FROM documents
            WHERE status = 'SUBMITTED' AND (locked_at IS NULL OR locked_at < NOW() - INTERVAL '10 MINUTE')
            ORDER BY id ASC
            LIMIT :batchSize
        )
        RETURNING id
        """, nativeQuery = true)
    List<Long> claimSubmittedBatch(@Param("batchSize") int batchSize);

}
