package com.helloegor03.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long documentId;
    private String actor;

    @Enumerated(EnumType.STRING)
    private DocumentAction action;

    private String comment;
    private LocalDateTime createdAt;
}
