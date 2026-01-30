package com.helloegor03.mapper;

import com.helloegor03.api.HistoryDto;
import com.helloegor03.domain.DocumentHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    HistoryDto toDto(DocumentHistory history);
}
