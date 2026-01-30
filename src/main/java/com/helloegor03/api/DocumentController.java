package com.helloegor03.api;

import com.helloegor03.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public Long create(@RequestBody CreateDocumentRequest request) {
        return documentService.create(request);
    }

    @GetMapping("/{id}")
    public DocumentWithHistoryResponse getOne(@PathVariable Long id) {
        return documentService.getWithHistory(id);
    }

    @GetMapping
    public Page<DocumentResponse> getBatch(
            @RequestParam List<Long> ids,
            Pageable pageable
    ) {
        return documentService.getBatch(ids, pageable);
    }

    @PostMapping("/submit")
    public List<BatchResultDto> submit(
            @RequestBody List<Long> ids,
            @RequestParam String actor
    ) {
        return documentService.submitBatch(ids, actor);
    }

    @PostMapping("/approve")
    public List<BatchResultDto> approve(
            @RequestBody List<Long> ids,
            @RequestParam String actor
    ) {
        return documentService.approveBatch(ids, actor);
    }

    @PostMapping("/search")
    public Page<DocumentResponse> search(
            @RequestBody SearchRequest request,
            Pageable pageable
    ) {
        return documentService.search(request, pageable);
    }
}