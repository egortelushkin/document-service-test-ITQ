package com.helloegor03.workers;

import com.helloegor03.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentWorkers {

    private final DocumentService documentService;

    @Value("${batch.size}")
    private int batchSize;

    @Scheduled(fixedDelay = 20_000)
    public void submitDraftDocuments() {
        List<Long> draftIds = documentService.submitBatchAtomic(batchSize, "AutoWorker");

        if (!draftIds.isEmpty()) {
            System.out.println("[" + LocalDateTime.now() + "] SUBMIT batch finished. Docs: " + draftIds);
        }
    }

    @Scheduled(fixedDelay = 25_000)
    public void approveSubmittedDocuments() {
        List<Long> submittedIds = documentService.approveBatchAtomic(batchSize, "AutoManager");

        if (!submittedIds.isEmpty()) {
            System.out.println("[" + LocalDateTime.now() + "] APPROVE batch finished. Docs: " + submittedIds);
        }
    }
}
