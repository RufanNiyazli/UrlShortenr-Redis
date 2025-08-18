package com.project.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "url")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;
    private Long clickCount;
}
