package com.project.urlshortener.dto;

public record UrlStats(String originalUrl,
                       String shortCode,
                       Long totalClicks) {
}
