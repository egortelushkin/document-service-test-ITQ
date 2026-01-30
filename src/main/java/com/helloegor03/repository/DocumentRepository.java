package com.helloegor03.repository;

import com.helloegor03.domain.Document;
import com.helloegor03.domain.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @Query("select d.id from Document d where d.status = :status")
    Page<Long> findIdsByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    Page<Document> findAllByIdIn(List<Long> ids, Pageable pageable);
}
