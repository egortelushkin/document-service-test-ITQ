package com.helloegor03.api;

import java.util.List;

public record DocumentWithHistoryResponse(
        DocumentResponse document,
        List<HistoryDto> history
) {}