package com.project.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UrlRequest(
        @NotBlank(message = "Url can't be empty!")
        @Pattern(regexp = "^(http|https)://.*$", message = "URL must start with http")
        String originalUrl,
        String customCode
) {
}

