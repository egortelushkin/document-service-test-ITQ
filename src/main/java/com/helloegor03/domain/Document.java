package com.helloegor03.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String number;

    private String author;
    private String title;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Version
    private Long version; // for optimistic locking

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

}
