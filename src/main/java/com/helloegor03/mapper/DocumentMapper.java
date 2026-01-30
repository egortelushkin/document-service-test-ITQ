package com.helloegor03.mapper;

import com.helloegor03.api.CreateDocumentRequest;
import com.helloegor03.api.DocumentResponse;
import com.helloegor03.domain.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    DocumentResponse toDto(Document document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Document fromCreateRequest(CreateDocumentRequest request);
}
