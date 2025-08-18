package com.project.urlshortener.dto;

public record UrlResponse(
        String originalUrl,
        String shorturl,
        String shortCode
        ) {


}
